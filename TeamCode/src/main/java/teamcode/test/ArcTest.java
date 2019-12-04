package teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.league2.DriveSystemLeague2;

@Autonomous(name = "Arc Test")
public class ArcTest extends AbstractOpMode {

    private DriveSystemLeague2 driveSystem;
    //private TTArmSystem arm;

    @Override
    protected void onInitialize() {
        //arm = new TTArmSystem(this);
        driveSystem = new DriveSystemLeague2(hardwareMap);
    }

    @Override
    protected void onStart() {
        Debug.log("here");
        // driveSystem.frontArc(true, 0.6,90, 9);
        while (opModeIsActive());
    }

    @Override
    protected void onStop() {

    }
}
