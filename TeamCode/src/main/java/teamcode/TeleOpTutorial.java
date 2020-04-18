package teamcode;

import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;

public class TeleOpTutorial extends AbstractOpMode {

    //the robot this teleOp was modelled after https://www.reddit.com/r/FTC/comments/feat97/that_other_robot_has_a_very_similar_drive_train/

    //also note ALL these numbers are arbetrary, both in here and the arm class, 
    private static final double INTAKE_POWER = 1.0;
    private static final double WINCH_MOTOR_POWER = 0.8;
    DriveSystemTutorial drive;
    ArmTutorial arm;
    Thread armUpdate;
    Thread driveUpdate;
    private final double NORMAL_SPEED_MODIFIER = 0.7;

    @Override
    protected void onInitialize() {
        drive = new DriveSystemTutorial(hardwareMap);
        arm = new ArmTutorial(hardwareMap);
        armUpdate = new Thread(){
            public void run(){
                while(AbstractOpMode.currentOpMode().opModeIsActive()){
                    armUpdate();
                }
            }
        };
        driveUpdate = new Thread(){
          public void run(){
              while(opModeIsActive()){
                  driveUpdate();
              }
          }
        };

    }

    private void driveUpdate() {
        Vector2D velocity = new Vector2D(gamepad1.right_stick_x, gamepad1.right_stick_y);
        if(!gamepad1.left_bumper){
            velocity.multiply(NORMAL_SPEED_MODIFIER);
        }
        drive.continous(velocity, gamepad1.left_stick_x);
    }


    /*
    intake
    off intake (same button)
    lifts us x inches
    manual adjustments (much smaller)
    turn the grabber
    radial arm movement
     */
    private void armUpdate() {
        if(gamepad1.right_trigger > 0.3){
            if(arm.getIntakePower()[0] == 0 && arm.getIntakePower()[1] == 0){
                arm.startIntake(INTAKE_POWER);
            }else{
                arm.stopIntake();
            }
        }else if(gamepad1.a){
            arm.lift(4, WINCH_MOTOR_POWER);
        }else if(gamepad1.b){
            arm.lift(0.2, WINCH_MOTOR_POWER);
        }else if(gamepad1.x){
            arm.adjustGrabber();
        }else if(gamepad1.y){
            arm.adjustRadialArm();
        }else if(gamepad1.dpad_down){
            arm.goHome();
        }
    }

    @Override
    protected void onStart() {
        driveUpdate.start();
        armUpdate.start();
    }

    @Override
    protected void onStop() {

    }
}
