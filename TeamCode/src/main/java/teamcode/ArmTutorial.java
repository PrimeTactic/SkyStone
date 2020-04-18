package teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

class ArmTutorial {
    private static final int WINCH_TICK_TOLERANCE = 40;
    private static final double GRABBER_CLOSE_POSITION = 0;
    private static final double GRABBER_OPEN_POSITION = 1;
    private static final double RADIAL_CLOSE_POSITION = 1;
    private static final double RADIAL_OPEN_POSITION = 0;
    Servo grabber, radialArm;
    DcMotor leftIntake, rightIntake, winchMotor;

    private final double WINCH_MOTOR_INCHES_TO_TICKS = 100;

    public ArmTutorial(HardwareMap hardwareMap){
        grabber = hardwareMap.servo.get("grabber");
        radialArm = hardwareMap.servo.get("radialArm");
        leftIntake = hardwareMap.dcMotor.get("leftIntake");
        rightIntake = hardwareMap.dcMotor.get("rightIntake");
        winchMotor = hardwareMap.dcMotor.get("winchMotor");
        winchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        winchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

    }

    public void stopIntake() {
        leftIntake.setPower(0);
        rightIntake.setPower(0);
    }

    public void startIntake(double intakePower){
        leftIntake.setPower(intakePower);
        rightIntake.setPower(intakePower);
    }


    public double[] getIntakePower() {
        return new double[]{leftIntake.getPower(), rightIntake.getPower()};
    }

    public void lift(double inches, double power) {
        int currentTicks = winchMotor.getCurrentPosition();
        int deltaTicks = (int)(inches * WINCH_MOTOR_INCHES_TO_TICKS);
        int targetTicks = deltaTicks + currentTicks;
        winchMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        winchMotor.setTargetPosition(targetTicks);
        winchMotor.setPower(power);
        while(isNearTarget());
        winchMotor.setPower(0);
    }

    private boolean isNearTarget() {
        return winchMotor.getTargetPosition() - winchMotor.getCurrentPosition() > WINCH_TICK_TOLERANCE;
    }

    public void adjustGrabber() {
        if(grabber.getPosition() == GRABBER_CLOSE_POSITION){
            grabber.setPosition(GRABBER_OPEN_POSITION);
        }else{
            grabber.setPosition(GRABBER_CLOSE_POSITION);
        }
    }

    public void adjustRadialArm() {
        if(radialArm.getPosition() == RADIAL_CLOSE_POSITION){
            radialArm.setPosition(RADIAL_OPEN_POSITION);
        }else{
            radialArm.setPosition(RADIAL_CLOSE_POSITION);
        }
    }

    public void goHome() {
        winchMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        winchMotor.setTargetPosition(0);
        winchMotor.setPower(-1);
        while(isNearTarget());
        winchMotor.setPower(0);
    }
}
