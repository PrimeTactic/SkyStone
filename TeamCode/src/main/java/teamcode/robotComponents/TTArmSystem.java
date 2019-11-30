package teamcode.robotComponents;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Timer;
import java.util.TimerTask;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Utils;

import static teamcode.common.Utils.sleep;

public class TTArmSystem {

    private static final double LIFT_INCHES_TO_TICKS = 300.0;
    private static final double LIFT_POSITION_ERROR_TOLERANCE = 100;

    private static final double WRIST_EXTENDED_POSITION = 0.0;
    private static final double WRIST_RETRACTED_POSITION = 1.0;
    private static final double WRIST_POSITION_ERROR_TOLERANCE = 0.05;
    private static final double WRIST_TICK_DELTA = -0.05;

    private static final double CLAW_OPEN_POSITION = 0.4;
    private static final double CLAW_CLOSE_POSITION = 1.0;
    private static final double CLAW_POSITION_ERROR_TOLERANCE = 0.05;

    private final DcMotor lift;
    private final Servo wrist, claw, leftFoundationGrabber, rightFoundationGrabber;
    private final DcMotor leftIntake, rightIntake;
    private final ColorSensor intakeSensor;

    private final AbstractOpMode opMode;

    public TTArmSystem(AbstractOpMode opMode) {
        HardwareMap hardwareMap = opMode.hardwareMap;
        lift = hardwareMap.get(DcMotor.class, TTHardwareComponentNames.ARM_LIFT);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftIntake = hardwareMap.get(DcMotor.class, TTHardwareComponentNames.INTAKE_LEFT);
        rightIntake = hardwareMap.get(DcMotor.class, TTHardwareComponentNames.INTAKE_RIGHT);
        wrist = hardwareMap.get(Servo.class, TTHardwareComponentNames.ARM_WRIST);
        claw = hardwareMap.get(Servo.class, TTHardwareComponentNames.ARM_CLAW);
        intakeSensor = hardwareMap.get(ColorSensor.class, TTHardwareComponentNames.INTAKE_SENSOR);
        leftFoundationGrabber = hardwareMap.get(Servo.class, TTHardwareComponentNames.LEFT_FOUNDATION_GRABBER);
        rightFoundationGrabber = hardwareMap.get(Servo.class, TTHardwareComponentNames.RIGHT_FOUNDATION_GRABBER);
        this.opMode = opMode;
    }

    public void setLiftHeight(double inches, double power) {
        int ticks = (int) (inches * LIFT_INCHES_TO_TICKS);
        lift.setTargetPosition(ticks);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift.setPower(power);
        while (!liftIsNearTarget()) ;
        lift.setPower(0.0);
    }

    public void liftContinuously(double power) {
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift.setPower(power);
    }

    private boolean liftIsNearTarget() {
        int target = lift.getTargetPosition();
        int current = lift.getCurrentPosition();
        double ticksFromTarget = Math.abs(target - current);
        return ticksFromTarget <= LIFT_POSITION_ERROR_TOLERANCE;
    }

    public boolean wristIsExtended() {
        return Utils.servoNearPosition(wrist, WRIST_EXTENDED_POSITION,
                WRIST_POSITION_ERROR_TOLERANCE);
    }

    public void setWristPosition(boolean extended) {
        if (extended) {
            wrist.setPosition(WRIST_EXTENDED_POSITION);
        } else {
            wrist.setPosition(WRIST_RETRACTED_POSITION);
        }
    }

    /**
     * IMPORTANT: BE SURE TO UPDATE RELATIONAL OPERATOR IF RETRACTED AND EXTENDED POSITIONS CHANGE.
     */
    public void extendWristIncrementally() {
        double currentPosition = WRIST_RETRACTED_POSITION;
        while (currentPosition >= WRIST_EXTENDED_POSITION) {
            currentPosition += WRIST_TICK_DELTA;
            wrist.setPosition(currentPosition);
            sleep(100);
        }
    }

    /**
     * Moves the arm components into the posistion which they can be easily scored
     */
    //4,5
    public void moveToScoringPos() {
        intake(0);
        //setClawPosition(false);
        //sleep(2000);
        Timer wrist = opMode.getNewTimer();
        TimerTask wristTask = new TimerTask(){
            @Override
            public void run(){
                setWristPosition(true);
            }
        };
        wrist.schedule(wristTask, 100);
        setLiftHeight(2100, 1);
        setLiftHeight(-1500, -1);
        setClawPosition(true);
    }

    public boolean clawIsOpen() {
        return Utils.servoNearPosition(claw, CLAW_OPEN_POSITION, CLAW_POSITION_ERROR_TOLERANCE);
    }

    public void setClawPosition(boolean open) {
        if (open) {
            claw.setPosition(CLAW_OPEN_POSITION);
        } else {
            claw.setPosition(CLAW_CLOSE_POSITION);
        }
    }

    /**
     * @param power sucks if positive, spits if negative
     */
    public void intake(double power) {
        power = -power;
        leftIntake.setPower(power);
        rightIntake.setPower(power);
    }

    public boolean intakeIsFull() {
        intakeSensor.enableLed(true);
        int blue = intakeSensor.blue();
        int red = intakeSensor.red();
        int green = intakeSensor.green();
        Debug.log("Red: " + red);
        Debug.log("Green: " + green);
        Debug.log("Blue: " + blue);
        return red > 2000 && green > 4000;
    }

    public void setFoundationGrabberPosition(boolean open) {
        if (open) {
            leftFoundationGrabber.setPosition(1);
            rightFoundationGrabber.setPosition(0);
        } else {
            leftFoundationGrabber.setPosition(0);
            rightFoundationGrabber.setPosition(1);
        }
    }

}
