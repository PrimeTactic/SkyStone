package teamcode.league3;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Utils;
import teamcode.common.Vector2D;

public class DriveSystem {

    private final DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private final GPS gps;

    private DcMotor[] motors;
    /**
     * The position in inches that the robot should try to reach. The target must be stored separate from the
     * GPS's current location due to errors in positioning that may accumulate.
     */
    private Vector2D targetPosition;
    /**
     * The bearing that the robot should try to reach in degrees. The target must be stored separate from the
     * GPS's current rotation due to errors in rotation that may accumulate.
     */
    private double targetRotation;

    private DriveMotion motion;

    public DriveSystem(HardwareMap hardwareMap, GPS gps, Vector2D currentPosition, double currentRotation) {
        frontLeft = hardwareMap.dcMotor.get(Constants.FRONT_LEFT_DRIVE_NAME);
        frontRight = hardwareMap.dcMotor.get(Constants.FRONT_RIGHT_DRIVE_NAME);
        rearLeft = hardwareMap.dcMotor.get(Constants.REAR_LEFT_DRIVE_NAME);
        rearRight = hardwareMap.dcMotor.get(Constants.REAR_RIGHT_DRIVE_NAME);
        correctDirections();
        this.gps = gps;
        targetPosition = currentPosition;
        targetRotation = currentRotation;
        motion = DriveMotion.STOP;
        motors = new DcMotor[]{frontLeft, frontRight, rearLeft, rearRight};
    }

    private void correctDirections() {
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void continuous(Vector2D velocity, double turnSpeed) {
        // Not sure why this is necessary, but it works. If it ain't broke, don't fix it.
        Vector2D velocity0 = new Vector2D(-velocity.getX(), velocity.getY());
        double direction = velocity0.getDirection();

        double maxPow = Math.sin(Math.PI / 4);
        double power = velocity.magnitude() / maxPow;

        double angle = direction - Math.PI / 4;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        frontLeft.setPower(power * sin + turnSpeed);
        frontRight.setPower(power * cos - turnSpeed);
        rearLeft.setPower(power * cos + turnSpeed);
        rearRight.setPower(power * sin - turnSpeed);
    }

    public void goTo(Vector2D targetPosition, double speed) {
        this.targetPosition = targetPosition;
        while (!near(targetPosition, targetRotation) && AbstractOpMode.currentOpMode().opModeIsActive()) {
            Vector2D currentPosition = gps.getPosition();
            double currentRotation = gps.getRotation();
            Vector2D translation = targetPosition.subtract(currentPosition);

            // Reduce power when approaching target position.
            double distanceToTarget = translation.magnitude();
            double power = getModulatedPower(speed, distanceToTarget);

            // Account for the orientation of the robot.
            Vector2D velocity = translation.normalize().multiply(power).rotate(Math.PI / 2 - currentRotation);

            double turnSpeed = Math.min((currentRotation - targetRotation) *
                    Constants.TURN_CORRECTION_SPEED_MULTIPLIER * speed, Constants.MAX_TURN_CORRECTION_SPEED * speed);

            continuous(velocity, turnSpeed);
        }
    }

    private double getModulatedPower(double maxSpeed, double distanceToTarget) {
        if (distanceToTarget < Constants.DRIVE_SPEED_REDUCTION_DISTANCE_INCHES) {
            return Math.min(maxSpeed, Utils.lerp(Constants.DRIVE_MIN_REDUCED_SPEED,
                    1, distanceToTarget /
                            Constants.DRIVE_SPEED_REDUCTION_DISTANCE_INCHES));
        } else {
            return maxSpeed;
        }
    }

    public void vertical(double inches, double speed) throws InterruptedException {
        int ticks = (int)(Constants.ODOMETER_INCHES_TO_TICKS * inches);
        gps.getLeftVertical().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        gps.getRightVertical().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        gps.getLeftVertical().setTargetPosition(ticks);
        gps.getRightVertical().setTargetPosition(ticks);
        gps.getHorizontal().setTargetPosition(0);
        gps.getLeftVertical().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        gps.getRightVertical().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        gps.getRightVertical().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower(speed);
        frontRight.setPower(speed);
        rearLeft.setPower(speed);
        rearRight.setPower(speed);
        while(!nearTarget()){
            Debug.log("Current Tick Left: " + gps.getLeftVertical().getCurrentPosition());
            Debug.log("Target Tick Left" + gps.getLeftVertical().getTargetPosition());
            Debug.log("Current Tick Right: " + gps.getRightVertical().getCurrentPosition());
            Debug.log("Target Tick Right" + gps.getRightVertical().getTargetPosition());
            Thread.sleep(200);

        }
        brake();
    }
    public void lateral(double inches, double speed) {
        int ticks = (int)(Constants.ODOMETER_INCHES_TO_TICKS * inches);
        gps.getHorizontal().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        gps.getHorizontal().setTargetPosition(-ticks);
        gps.getHorizontal().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower(speed);
        frontRight.setPower(-speed);
        rearLeft.setPower(-speed);
        rearRight.setPower(speed);
        while(!nearTarget());
        brake();
    }

    public void turn(double degrees, double speed) {
        int ticks = (int)(Constants.ODOMETER_DEGREES_TO_TICKS * degrees);
        gps.getHorizontal().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        gps.getHorizontal().setTargetPosition(ticks);
        gps.getHorizontal().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower(speed);
        frontRight.setPower(-speed);
        rearLeft.setPower(speed);
        rearRight.setPower(-speed);
        while(!nearTarget());
        brake();
    }

    public enum DriveMotion{
        VERTICAL, LATERAL, TURN, STOP
    }

    public void brake() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        rearLeft.setPower(0);
        rearRight.setPower(0);
    }

    /**
     * Returns true if the robot is near the specified position and rotation, false otherwise.
     */
    private boolean near(Vector2D position, double rotation) {
        Vector2D currentPosition = gps.getPosition();
        Vector2D positionOffset = position.subtract(currentPosition);
        Debug.clear();
        Debug.log("offset: " + positionOffset);
        double currentRotation = gps.getRotation();
        double rotationOffset = rotation - currentRotation;
        return Math.abs(positionOffset.getX()) < Constants.DRIVE_OFFSET_TOLERANCE_INCHES &&
                Math.abs(positionOffset.getY()) < Constants.DRIVE_OFFSET_TOLERANCE_INCHES;// &&
        //Math.abs(Math.toDegrees(rotationOffset)) < Constants.DRIVE_OFFSET_TOLERANCE_DEGREES;
    }

    //to only be used for hardpaths, once goTo is fully implemented we should not use this
    private boolean nearTarget(){
        return (Math.abs(gps.getLeftVertical().getTargetPosition() - gps.getLeftVertical().getCurrentPosition()) < Constants.DRIVE_TOLERANCE_TICKS ||
                Math.abs(gps.getRightVertical().getTargetPosition() - gps.getRightVertical().getCurrentPosition()) < Constants.DRIVE_TOLERANCE_TICKS) &&
                Math.abs(gps.getHorizontal().getTargetPosition() - gps.getHorizontal().getCurrentPosition()) < Constants.DRIVE_TOLERANCE_TICKS;

    }

    public DcMotor[] getMotors(){
        return motors;
    }

}
