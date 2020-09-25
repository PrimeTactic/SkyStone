package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;

import teamcode.common.AbstractOpMode;
import teamcode.common.Localizer;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Point;
import teamcode.common.PurePursuit.CurvePoint;
import teamcode.common.PurePursuit.MovementVars;
import teamcode.common.PurePursuit.PurePursuitMovement;
import teamcode.common.Vector2D;

@TeleOp(name="OdoPushTest")
public class OdometryDiagnostic extends AbstractOpMode {

    Localizer localizer;
    PurePursuitMovement movement;
    MecanumDriveTrain drive;
    ArrayList<CurvePoint> allPoints;

    @Override
    protected void onInitialize() {
        drive = new MecanumDriveTrain(hardwareMap);
        localizer = new Localizer(hardwareMap, new Point(0,0), 0);
        movement = new PurePursuitMovement(localizer);
        allPoints = new ArrayList<>();
        allPoints.add(new CurvePoint(0, 0, 0.5, 0.3, 5, 0, 1));
        //allPoints.add(new CurvePoint(25, 0, 0.5, 0.3, 5, 0, 1));
        allPoints.add(new CurvePoint(25, 25, 0.5, 0.3, 5, 0, 1));
        //allPoints.add(new CurvePoint(0, 25, 0.5, 0.3, 5, 0, 1));

    }

    @Override
    protected void onStart() {
        new Thread(){
            @Override
            public void run(){
                while(opModeIsActive()){
                    drive.setPowerPurePursuit(new Vector2D(MovementVars.movementX, MovementVars.movementY), MovementVars.movementTurn);
                }
            }
        }.start();
        while(opModeIsActive()){

            movement.followCurve(allPoints, 0);
            telemetry.addData("CurrentPos", localizer.getCurrentPosition());
            telemetry.addData("CurrentRads", localizer.getGlobalRads());
            telemetry.addData("CurrentIndex", movement.getCurrentRobotIndex());
            telemetry.addData("MovementX", MovementVars.movementX);
            telemetry.addData("MovementY", MovementVars.movementY);
            telemetry.addData("MovementTurn", MovementVars.movementTurn);
            telemetry.update();
        }
    }

    @Override
    protected void onStop() {

    }
}
