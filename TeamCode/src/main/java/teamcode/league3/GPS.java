package teamcode.league3;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;


import teamcode.common.Vector2D;

/**
 * Always call shutdown() in onStop() of AbstractOpMode.
 */
public class GPS {

    /**
     * Whether or not this GPS should continue to update positions.
     */
    private boolean active;
    private Vector2D position;
    /**
     * In radians, unit circle style.
     */
    private double rotation;
    private final DcMotor leftVertical, rightVertical, horizontal;
    private double prevLeftVerticalPos, prevRightVerticalPos, prevHorizontalPos;
    private long lastUpdateTime;


    public GPS(HardwareMap hardwareMap, Vector2D currentPosition, double currentDirection) {
        active = true;
        this.position = currentPosition;
        this.rotation = currentDirection;
        leftVertical = hardwareMap.dcMotor.get(Constants.LEFT_VERTICAL_ODOMETER);
        rightVertical = hardwareMap.dcMotor.get(Constants.RIGHT_VERTICAL_ODOMETER);
        horizontal = hardwareMap.dcMotor.get(Constants.HORIZONTAL_ODOMETER);
        prevLeftVerticalPos = 0;
        prevRightVerticalPos = 0;
        prevHorizontalPos = 0;
        correctDirections();
        resetEncoders();
        Thread positionUpdater = new Thread() {
            @Override
            public void run() {
                while (active) {
                    //updateArcBasedLocation();
                    updateLocation();
                }
            }
        };
        positionUpdater.start();
    }

    private void correctDirections() {
        leftVertical.setDirection(DcMotorSimple.Direction.REVERSE);
        rightVertical.setDirection(DcMotorSimple.Direction.REVERSE);
        horizontal.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    private void resetEncoders() {
        leftVertical.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightVertical.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        horizontal.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }







    private synchronized void updateLocation() {
        double leftVerticalPos = leftVertical.getCurrentPosition();
        double rightVerticalPos = rightVertical.getCurrentPosition();
        double deltaLeftVertical = leftVerticalPos - prevLeftVerticalPos;
        double deltaRightVertical = rightVerticalPos - prevRightVerticalPos;

        double deltaRot = (deltaLeftVertical - deltaRightVertical) / 50; //Constants.ODOMETRY_WHEEL_DISTANCE;
        rotation += deltaRot;

        double horizontalPos = horizontal.getCurrentPosition() - deltaRot * 2200; // HORIZONTAL_DEGREES_TO_TICKS;
        double deltaHorizontal = horizontalPos - prevHorizontalPos;

        double p = (deltaRightVertical + deltaLeftVertical) / 2;
        double n = deltaHorizontal;

        double x = position.getX() + p * Math.sin(rotation) + n * Math.cos(rotation);
        double y = position.getY() + p * Math.cos(rotation) + n * Math.sin(rotation);
        position.setX(x);
        position.setY(y);

        prevLeftVerticalPos = leftVerticalPos;
        prevRightVerticalPos = rightVerticalPos;
        prevHorizontalPos = horizontalPos;
    }

    /**
     * Returns the position of the robot as read by the odometers. In inches
     */
    public synchronized Vector2D getPosition() {
        return position.multiply(1 / Constants.ODOMETER_INCHES_TO_TICKS);
    }

    /**
     * Returns the rotation in radians, unit circle style.
     */
    public synchronized double getRotation() {
        return rotation;
    }


    private double radiansToBearing(double radians) {
        return 0;
    }

    private double bearingToRadians(double bearing) {
        return 0;
    }

    /**
     * Invoke this in AbstractOpMode.onStop().
     */
    public void shutdown() {
        active = false;
    }

}
