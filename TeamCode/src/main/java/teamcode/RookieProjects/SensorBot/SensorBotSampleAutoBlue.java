package teamcode.RookieProjects.SensorBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import teamcode.RookieProjects.SensorBot.SensorBot;
import teamcode.RookieProjects.SensorBot.SensorBotDriveTrain;
import teamcode.common.AbstractOpMode;
import teamcode.common.Vector2D;
import teamcode.test.AdvancedOdometry.ArcPositionUpdate;
import teamcode.test.AdvancedOdometry.CurvePoint;
import teamcode.test.AdvancedOdometry.MovementVars;
import teamcode.test.AdvancedOdometry.Point;
import teamcode.test.AdvancedOdometry.PurePursuitMovement;

public class SensorBotSampleAutoBlue extends AbstractOpMode{
    SensorBot bot;
    SensorBotDriveTrain drive;
    PurePursuitMovement movement;
    ArcPositionUpdate localizer;

    String path;

    @Override
    protected void onInitialize() {
        bot = new SensorBot(hardwareMap);
        drive = new SensorBotDriveTrain(hardwareMap);
        localizer = new ArcPositionUpdate(hardwareMap, new Point(9, 35), 0);
        movement = new PurePursuitMovement(localizer);
    }

    @Override
    protected void onStart() {
        ArrayList<CurvePoint> path =  new ArrayList<>();
        MovementVars.movementX = 0;
        MovementVars.movementY = 0;
        MovementVars.movementTurn = 0;
        new Thread(){
            public void run(){
                while(opModeIsActive()){
                    drive.continuous(new Vector2D(MovementVars.movementX, MovementVars.movementY), MovementVars.movementTurn);
                }
            }
        }.start();
        //path.add() points for the opMode
        try {
            movement.followCurve(path, 90);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        File file = new File(path);
        try {
            PrintStream fileScanner = new PrintStream(file);
            fileScanner.print(localizer.getCurrentPosition() + " GlobalRads: " + localizer.getGlobalRads());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
