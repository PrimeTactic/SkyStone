package DemoCodes.common;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import DemoCodes.common.DemoComponentNames;

public class DemoArm {

    private static final double CLAW_OPEN_POS = 1.0;
    private static final double CLAW_CLOSE_POS = 0.0;
    private static final double CLAW_POSITION_ERROR = 0.1;

    private DcMotor armLift;
    private Servo claw;

    public DemoArm(HardwareMap hardwareMap){
        armLift = hardwareMap.get(DcMotor.class, DemoComponentNames.ARM_LIFT);
        claw = hardwareMap.get(Servo.class, DemoComponentNames.CLAW);
    }

    public void raise(){
        armLift.setPower(0.5);
    }

    public void lower(){
        armLift.setPower(-0.5);
    }

    public void stop(){
        armLift.setPower(0);
    }

    public void openClaw() {
        claw.setPosition(CLAW_OPEN_POS);
    }

    public void closeClaw() {
        claw.setPosition(CLAW_CLOSE_POS);
    }

    public boolean clawIsOpen() {
        return Math.abs(claw.getPosition() - CLAW_OPEN_POS) < CLAW_POSITION_ERROR;
    }


    public boolean clawIsClose(){
        return Math.abs(claw.getPosition() - CLAW_CLOSE_POS) < CLAW_POSITION_ERROR;
    }

    public Servo getClaw(){
        return claw;
    }
}