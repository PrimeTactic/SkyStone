package teamcode.league3;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.common.AbstractOpMode;
import teamcode.common.Utils;
import teamcode.common.Vector2D;

public class DriveSystem {

    private static final double SPEED_REDUCTION_THRESHOLD_INCHES = 48;
    private static final double TURN_SPEED_REDUCTION_THRESHOLD_RADIANS = 0.0523599;
    private static final double MIN_REDUCED_SPEED = 0.4;
    private static final double INCHES_OFFSET_TOLERANCE = 1;
    private static final double RADIANS_OFFSET_TOLERANCE = Math.toRadians(3);
    private static final double TURN_CORRECTION_SPEED_MULTIPLIER = 1;
    private static final double MAX_TURN_CORRECTION_SPEED = 0.1;

    private final DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private final GPS gps;
    /**
     * The position in inches that the robot should try to reach. The target must be stored separate from the
     * GPS's current location due to errors in positioning that may accumulate.
     */
    private Vector2D targetPosition;
    /**
     * In radians
     */
    private double targetRotation;

    /**
     * @param hardwareMap
     * @param gps
     * @param currentPosition in inches
     * @param currentRotation in radians
     */
    public DriveSystem(HardwareMap hardwareMap, GPS gps, Vector2D currentPosition, double currentRotation) {
        frontLeft = hardwareMap.dcMotor.get(Constants.FRONT_LEFT_DRIVE_NAME);
        frontRight = hardwareMap.dcMotor.get(Constants.FRONT_RIGHT_DRIVE_NAME);
        rearLeft = hardwareMap.dcMotor.get(Constants.REAR_LEFT_DRIVE_NAME);
        rearRight = hardwareMap.dcMotor.get(Constants.REAR_RIGHT_DRIVE_NAME);

        initMotors();
        this.gps = gps;
        targetPosition = currentPosition;
        targetRotation = currentRotation;
    }

    private void initMotors() {
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /**
     * @param velocity
     * @param turnSpeed counterclockwise if positive
     */
    public void continuous(Vector2D velocity, double turnSpeed) {
        // Not sure why this is necessary, but it works. If it ain't broke, don't fix it.
        Vector2D velocity0 = new Vector2D(-velocity.getX(), velocity.getY());
        double direction = velocity0.getDirection();

        double maxPow = Math.sin(Math.PI / 4);
        double power = velocity.magnitude() / maxPow;

        double angle = direction - Math.PI / 4;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        frontLeft.setPower(power * sin - turnSpeed);
        frontRight.setPower(power * cos + turnSpeed);
        rearLeft.setPower(power * cos - turnSpeed);
        rearRight.setPower(power * sin + turnSpeed);
    }

    /**
     * @param targetPosition in inches
     * @param speed          [0, 1]
     */
    public void goTo(Vector2D targetPosition, double speed) {
        this.targetPosition = targetPosition;
        Vector2D startPosition = gps.getPosition();
        double maxTurnSpeed = MAX_TURN_CORRECTION_SPEED * speed;
        while (!near(targetPosition, targetRotation) && AbstractOpMode.currentOpMode().opModeIsActive()) {
            Vector2D currentPosition = gps.getPosition();
            double currentRotation = gps.getRotation();
            Vector2D targetTranslation = targetPosition.subtract(currentPosition);

            // Reduce power when leaving start and approaching target position.
            double distanceFromStart = currentPosition.subtract(startPosition).magnitude();
            double distanceToTarget = targetTranslation.magnitude();
            double power = getModulatedLinearDrivePower(speed, distanceFromStart, distanceToTarget);

            // Account for the orientation of the robot.
            Vector2D velocity = targetTranslation.normalize().multiply(power).rotate(Math.PI / 2 - currentRotation);

            double turnSpeed = (targetRotation - currentRotation) *
                    TURN_CORRECTION_SPEED_MULTIPLIER * speed;
            if (Math.abs(turnSpeed) > maxTurnSpeed) {
                turnSpeed = Math.signum(turnSpeed) * maxTurnSpeed;
            }

            continuous(velocity, turnSpeed);
        }
    }

    private double getModulatedLinearDrivePower(double maxSpeed, double distanceFromStart, double distanceToTarget) {
        double accelerationPower;
        double decelerationPower;
        if (distanceFromStart < SPEED_REDUCTION_THRESHOLD_INCHES) {
            accelerationPower = Math.min(maxSpeed, Utils.lerp(MIN_REDUCED_SPEED,
                    1, distanceFromStart /
                            SPEED_REDUCTION_THRESHOLD_INCHES));
        } else {
            accelerationPower = maxSpeed;
        }
        if (distanceToTarget < SPEED_REDUCTION_THRESHOLD_INCHES) {
            decelerationPower = Math.min(maxSpeed, Utils.lerp(MIN_REDUCED_SPEED,
                    1, distanceToTarget /
                            SPEED_REDUCTION_THRESHOLD_INCHES));
        } else {
            decelerationPower = maxSpeed;
        }
        return Math.min(accelerationPower, decelerationPower);
    }

    public void vertical(double inches, double speed) {
        Vector2D translation = Vector2D.up().multiply(inches).rotate(targetRotation - Math.PI / 2);
        targetPosition = targetPosition.add(translation);
        goTo(targetPosition, speed);
    }

    public void lateral(double inches, double speed) {
        Vector2D translation = Vector2D.right().multiply(inches).rotate(targetRotation + Math.PI / 2);
        targetPosition = targetPosition.add(translation);
        goTo(targetPosition, speed);
    }

    /**
     * @param radians turns counterclockwise if positive
     * @param speed   [0, 1]
     */
    public void turn(double radians, double speed) {
        speed = Math.abs(speed);
        // Use the GPS location because turn does not correct linear movement. Using the target
        // position could result in an endless loop.
        double startRotation = gps.getRotation();
        targetRotation = startRotation + radians;
        while (!near(gps.getPosition(), targetRotation) && AbstractOpMode.currentOpMode().opModeIsActive()) {
            double currentRotation = gps.getRotation();
            double radiansFromStart = currentRotation - startRotation;
            double radiansToTarget = targetRotation - currentRotation;
            double signedSpeed = Math.signum(radiansToTarget) * speed;
            double power = getModulatedTurnPower(signedSpeed, radiansFromStart, radiansToTarget);
            continuous(Vector2D.zero(), power);
        }
    }

    private double getModulatedTurnPower(double maxSpeed, double radiansFromStart, double radiansToTarget) {
        double accelerationPower;
        double decelerationPower;
        if (radiansFromStart < TURN_SPEED_REDUCTION_THRESHOLD_RADIANS) {
            accelerationPower = Math.min(maxSpeed, Utils.lerp(MIN_REDUCED_SPEED,
                    1, radiansFromStart /
                            SPEED_REDUCTION_THRESHOLD_INCHES));
        } else {
            accelerationPower = maxSpeed;
        }
        if (radiansToTarget < SPEED_REDUCTION_THRESHOLD_INCHES) {
            decelerationPower = Math.min(maxSpeed, Utils.lerp(MIN_REDUCED_SPEED,
                    1, radiansToTarget /
                            TURN_SPEED_REDUCTION_THRESHOLD_RADIANS));
        } else {
            decelerationPower = maxSpeed;
        }
        return Math.min(accelerationPower, decelerationPower);
    }

    public void brake() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        rearLeft.setPower(0);
        rearRight.setPower(0);
    }

    /**
     * Returns true if the robot is near the specified position and rotation, false otherwise.
     *
     * @param position in inches
     * @param rotation in radians
     */
    private boolean near(Vector2D position, double rotation) {
        Vector2D currentPosition = gps.getPosition();
        Vector2D positionOffset = position.subtract(currentPosition);
        double currentRotation = gps.getRotation();
        double rotationOffset = rotation - currentRotation;
        return positionOffset.magnitude() < INCHES_OFFSET_TOLERANCE &&
                Math.abs(rotationOffset) < RADIANS_OFFSET_TOLERANCE;
    }

}
