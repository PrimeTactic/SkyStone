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
import teamcode.common.Utils;
import teamcode.test.revextensions2.ExpansionHubEx;
import teamcode.test.revextensions2.RevBulkData;

public class Shooter {
    private static final double INDEXER_EXTENDED_POS = 1.0;
    private static final double INDEXER_RETRACTED_POS = 0.0;
    /*
    Electronics Schematic:
    1x Motor Roller
    2x flywheel motor
    1xIndexing Actuator
    1x secondary Indexer valve
    this schematic assumes the through gravity indexer
     */

    DcMotor roller, leftFlywheel, rightFlywheel;
    Servo indexer;
    ExpansionHubEx hub;

    public Shooter(HardwareMap hardwareMap){
        roller = hardwareMap.dcMotor.get("rollerMotor");
        leftFlywheel = hardwareMap.dcMotor.get("leftFlywheel");
        rightFlywheel = hardwareMap.dcMotor.get("rightFlywheel");
        indexer = hardwareMap.servo.get("Indexer");
    }


    /**
     * generic intake for Tele Op
     * @param power power to run the intake
     */
    public void intake(double power){
        roller.setPower(power);
    }

    /**
     * runs intake for specified number of millis, for Auto
     * @param power power to run the intake
     * @param millis millis intake should run
     */
    public void intake(double power, long millis){
        roller.setPower(power);
        Utils.sleep(millis);
        roller.setPower(0);
    }


    public void shoot(){

        leftFlywheel.setPower(1.0);
        rightFlywheel.setPower(1.0); //calibrate this
        indexer.setPosition(INDEXER_EXTENDED_POS);
        try {
            Thread.sleep(200); // calibrate this
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        leftFlywheel.setPower(0);
        rightFlywheel.setPower(0);
        indexer.setPosition(INDEXER_RETRACTED_POS);
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

    /**
     * keeping this code for later, DO NOT DELETE OR CALL
     */

//    private void autoOrient(){
//        double distanceAwayX = Constants.GOAL_POSITION.x - Localizer.thisLocalizer().getCurrentPosition().x;
//        double distanceAwayY = Constants.GOAL_POSITION.y - Localizer.thisLocalizer().getCurrentPosition().y;
//        double angleDiff = Math.atan2(distanceAwayY, distanceAwayX);
//        drive.rotate(headingToDirectionRads(angleDiff), 0.4);
//        double distanceAway = Math.sqrt(Math.pow(distanceAwayX, 2) + Math.pow(distanceAwayY, 2));
//    }



    //TODO add endgame functions and game specific electronics
    //Wobble Grabber
}
