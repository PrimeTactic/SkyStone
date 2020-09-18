package teamcode.common;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.test.revextensions2.ExpansionHubEx;
import teamcode.test.revextensions2.ExpansionHubMotor;
import teamcode.test.revextensions2.RevBulkData;

import static java.lang.Math.*;

public class Localizer {
    //TODO before reading this file please note the static import of the math class,
    // odds are if you see a math function it is from that and not a constatnt/method I created
    //https://docs.google.com/ document/d/1JQuU2M--rVFEa9ApKkOcc0CalsBxKjH-l0PQLRW7F3c/edit?usp=sharing proof behind the math

    private static final double ODOMETER_TICKS_TO_INCHES = 1.0 / 1102.0;
    private static final double HORIZONTAL_ODOMETER_ROTATION_OFFSET_TICKS = 0.4;
    private static final double VERTICAL_ODOMETER_TICKS_TO_RADIANS = 0.00006714153;
    private static final int LEFT_VERTICAL_ODOMETER_DIRECTION = -1;
    private static final int RIGHT_VERTICAL_ODOMETER_DIRECTION = -1;
    private static final int HORIZONTAL_ODOMETER_DIRECTION = -1;
    private static final double TICKS_PER_REV = 8192;
    private static final double WHEEL_RADIUS = 1.181;
    private static final double GEAR_RATIO = 1;
    private static final double CHASSIS_LENGTH = 15.39;
    private static final double LENGTH_TOLERANCE = encoderTicksToInches(100);
    private static Localizer thisLocalizer;


    /**
     * Whether or not this GPS should continue to update positions.
     */
    private boolean active;
    /**
     * Position stored in ticks. When exposed to external classes, it is represented in inches.
     */
    private Point currentPosition;
    /**
     * In radians, as a direction.
     */
    private double globalRads;

    private final ExpansionHubMotor leftVertical, rightVertical, horizontal;
    private final ExpansionHubEx hub1 , hub2;
    private RevBulkData data1, data2;
    private final BNO055IMU imu;
    private double previousOuterArcLength;
    private double previousInnerArcLength;
    private double previousHorizontalArcLength;
    private double thetaGyro;
    /**
     * @param position in inches
     * @param globalRads in radians
     */
    public Localizer(HardwareMap hardwareMap, Point position, double globalRads) {
        hub1 = hardwareMap.get(ExpansionHubEx.class,"Expansion Hub 1");
        hub2 = hardwareMap.get(ExpansionHubEx.class,"Expansion Hub 2");
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        leftVertical = (ExpansionHubMotor)hardwareMap.dcMotor.get(Constants.LEFT_VERTICAL_ODOMETER_NAME);
        rightVertical = (ExpansionHubMotor)hardwareMap.dcMotor.get(Constants.RIGHT_VERTICAL_ODOMETER_NAME);
        horizontal = (ExpansionHubMotor)hardwareMap.dcMotor.get(Constants.HORIZONTAL_ODOMETER_NAME);
        //position.dilate(1.0 / ODOMETER_TICKS_TO_INCHES);
        this.globalRads = globalRads;
        this.currentPosition = position;
        resetEncoders();
        thisLocalizer = this;
        Thread positionCalculator = new Thread() {
            @Override
            public void run() {
                while (AbstractOpMode.currentOpMode().opModeIsActive()) {
                    update();
                }
            }
        };
        positionCalculator.start();
    }    

    private void resetEncoders() {
        leftVertical.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightVertical.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        horizontal.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        horizontal.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    //see top of class for formalized proof of the math
    private synchronized void update() {
        double innerArcLength = encoderTicksToInches(leftVertical.getCurrentPosition());
        double outerArcLength = encoderTicksToInches(rightVertical.getCurrentPosition());
        double horizontalArcLength = encoderTicksToInches(horizontal.getCurrentPosition());

        double deltaInnerArcLength = innerArcLength - previousInnerArcLength;
        double deltaOuterArcLength = outerArcLength - previousOuterArcLength;
        double deltaHorizontalArcLength = horizontalArcLength - previousHorizontalArcLength;

        double arcLength = (deltaInnerArcLength + deltaOuterArcLength) / 2.0;
        double phi = (deltaOuterArcLength - deltaInnerArcLength) / CHASSIS_LENGTH; //phi is defined as the change in angle of the arclength
        double hypotenuse;
        if(abs(phi) < 0.0001){
            //cornerCase to account for a non arc case, approximate arcLength equal to hypotenuse
            hypotenuse = arcLength;
        }else{
            hypotenuse = (arcLength * sin(phi)) / (phi * cos(phi / 2.0));
        }
        double newX = currentPosition.x + hypotenuse * cos((phi / 2.0) + globalRads) + deltaHorizontalArcLength * cos(globalRads + (phi / 2.0) - (PI / 2));
        double newY = currentPosition.y + hypotenuse * sin((phi / 2.0)+ globalRads) + deltaHorizontalArcLength * sin(globalRads + (phi / 2.0) - (PI / 2));
        globalRads += phi;
        currentPosition = new Point(newX, newY);
        previousInnerArcLength = innerArcLength;
        previousOuterArcLength = outerArcLength;
        previousHorizontalArcLength = horizontalArcLength;

    }


    public static double encoderTicksToInches(int ticks) {
        return WHEEL_RADIUS * 2 * PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }
    public static int inchesToEncoderTicks(double inches){
        return (int)((TICKS_PER_REV / (WHEEL_RADIUS * 2 * PI * GEAR_RATIO)) * inches);
    }

    public Point getCurrentPosition(){
        return currentPosition;
    }
    public double getGlobalRads(){
        return globalRads;
    }

    public static Localizer thisLocalizer(){
        return thisLocalizer;
    }



}
