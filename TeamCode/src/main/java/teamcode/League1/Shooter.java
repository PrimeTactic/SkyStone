package teamcode.League1;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import java.util.Timer;

import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Localizer;
import teamcode.common.MecanumDriveTrain;

public class Shooter {
    /*
    Electronics Schematic:
    1x Motor Roller
    2x flywheel motor
    2x angle adjustment Servo (Geared for 60 deg rot)
     */

    DcMotor roller, leftFlywheel, rightFlywheel;
    Servo leftAngle, rightAngle; //these servos should ALWAYS BE SYNCHRONIZED
    TouchSensor indexProgressor;

    public Shooter(HardwareMap hardwareMap){
        roller = hardwareMap.dcMotor.get("rollerMotor");
        leftFlywheel = hardwareMap.dcMotor.get("leftFlywheel");
        rightFlywheel = hardwareMap.dcMotor.get("rightFlywheel");
        leftAngle = hardwareMap.servo.get("leftAngler");
        rightAngle = hardwareMap.servo.get("rightAngler");
        indexProgressor = hardwareMap.touchSensor.get("indexer");
        resetHardware();
    }



    private void resetHardware() {
        setPosition(0);
    }

    private void setPosition(double position){
        leftAngle.setPosition(position);
        rightAngle.setPosition(position);
    }


    /**
     * input: 0, output: 0
     * input 60, output: 1
     * input: 30, output: 0.5
     * @param angle the angle which the shooter should be at in degrees
     *
     */
    private void setPositionAngular(double angle){
        double position = angle / 60.0;
        leftAngle.setPosition(position);
        rightAngle.setPosition(position);
    }

    public void intake(double power){
        while(AbstractOpMode.currentOpMode().opModeIsActive() && !indexProgressor.isPressed()) {
            roller.setPower(power);
        }
        roller.setPower(0);
    }

    public void shoot(MecanumDriveTrain drive){
        double distanceAwayX = Constants.GOAL_POSITION.x - Localizer.thisLocalizer().getCurrentPosition().x;
        double distanceAwayY = Constants.GOAL_POSITION.y - Localizer.thisLocalizer().getCurrentPosition().y;
        double angleDiff = Math.atan2(distanceAwayY, distanceAwayX);
        drive.rotate(headingToDirectionRads(angleDiff), 0.4);
        double distanceAway = Math.sqrt(Math.pow(distanceAwayX, 2) + Math.pow(distanceAwayY, 2));
        double angle = 0.5 * Math.asin((Constants.GRAVITY_IN_SEC * distanceAway) / (Math.pow(Constants.INITIAL_VELOCITY, 2)));
        setPositionAngular(angle);
        leftFlywheel.setPower(1.0);
        rightFlywheel.setPower(1.0);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        leftFlywheel.setPower(0);
        rightFlywheel.setPower(0);
        intake(0.5); //this one line is for a multi ball indexer
    }

    /*
     * Direction     Heading
     * 0             -90
     * 90            0
     * 180           90
     * 270           180
     *
     */
    private double headingToDirectionRads(double heading){
        return heading + (Math.PI / 2.0);
    }

    //TODO add endgame functions and game specific electronics
}
