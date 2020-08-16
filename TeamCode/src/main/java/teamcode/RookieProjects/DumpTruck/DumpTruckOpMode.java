package teamcode.RookieProjects.DumpTruck;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;

@TeleOp(name="DumpTruck")
public class DumpTruckOpMode extends AbstractOpMode {

    private static final double INTAKE_POWER = 0.7;
    DumpTruck intakeSystem;
    DumpTruckDriveTrain drive;

    Thread driveUpdate;
    Thread armUpdate;

    @Override
    protected void onInitialize() {
        drive = new DumpTruckDriveTrain(hardwareMap);
        intakeSystem = new DumpTruck(hardwareMap);
        driveUpdate = new Thread(){
            public void run(){
                while(opModeIsActive()){
                    driveUpdate();
                }
            }
        };
        armUpdate = new Thread(){
            public void run(){
                while(opModeIsActive()){
                    armUpdate();
                }
            }
        };
    }

    private void armUpdate() {
        if(gamepad1.x){
            intakeSystem.adjustDumpValues();
        }else if(gamepad1.right_trigger > 0.3){
            intakeSystem.intake(INTAKE_POWER);
        }else if(gamepad1.left_trigger >= 0){
            intakeSystem.intakeSimple(gamepad1.left_trigger);
        }
    }

    private void driveUpdate() {
        Vector2D velocity = new Vector2D(gamepad1.right_stick_x, gamepad1.right_stick_y);
        drive.continuous(velocity, gamepad1.left_stick_x);
    }

    @Override
    protected void onStart() {
        armUpdate.start();
        driveUpdate.start();
        while(opModeIsActive());
    }

    @Override
    protected void onStop() {

    }
}
