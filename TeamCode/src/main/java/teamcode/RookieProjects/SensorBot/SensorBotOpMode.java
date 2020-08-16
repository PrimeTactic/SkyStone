package teamcode.RookieProjects.SensorBot;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import teamcode.RookieProjects.SensorBot.SensorBot;
import teamcode.RookieProjects.SensorBot.SensorBotDriveTrain;
import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;

@TeleOp(name="SensorTest")
public class SensorBotOpMode extends AbstractOpMode {
    SensorBotDriveTrain drive;
    SensorBot bot;
    Color allianceColor;

    enum Color{
        RED,
        BLUE;
    }


    Thread driveThread; //drive code
    Thread robotThread; //things that arent drive related
    @Override
    protected void onInitialize() {
        try {
            Scanner fileScanner = new Scanner(new File(Constants.ODOMETRY_FILE_PATH));
            String point = fileScanner.next();
            String xValue = point.substring(1,2);
            String yValue = point.substring(3,4);
            double yValueNum = Double.parseDouble(yValue);
            if(yValueNum <= 72.0){
                allianceColor =  Color.BLUE;
            }else{
                allianceColor = Color.RED;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bot = new SensorBot(hardwareMap);
        drive = new SensorBotDriveTrain(hardwareMap);
        driveThread = new Thread(){
            @Override
            public void run(){
                while(opModeIsActive()){
                    driveUpdate();
                }
            }
        };
        robotThread = new Thread(){
            @Override
            public void run(){
                while(opModeIsActive()){
                    robotUpdate();
                }
            }
        };
    }

    private void robotUpdate() {
        int[] colorSensorValues = bot.updateColorSensorValues();
        if(colorSensorValues[2] >= 400 || colorSensorValues[0] >= 400){ //TODO needs to be adjusted to fit real world
            bot.adjustFlap();
        }
    }

    //version of the code that uses the Odometry to determine the alliance position
    private void robotUpdateOdo(){
        int[] colorSensorValues = bot.updateColorSensorValues();
        if(allianceColor == Color.BLUE) {
            if (colorSensorValues[2] >= 400) { //TODO needs to be adjusted to fit real world
                bot.adjustFlap();
            }
        }else{
            if(colorSensorValues[0] >= 400){
                bot.adjustFlap();
            }
        }
    }

    private void driveUpdate() {
        drive.continuous(new Vector2D(gamepad1.left_stick_x, gamepad1.left_stick_y), gamepad1.right_stick_y);
    }

    @Override
    protected void onStart() {
        driveThread.start();
        robotThread.start();
    }

    @Override
    protected void onStop() {
        driveThread.interrupt();
        robotThread.interrupt();
    }
}
