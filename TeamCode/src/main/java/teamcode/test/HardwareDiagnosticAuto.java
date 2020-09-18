package teamcode.test;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Localizer;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Point;

public class HardwareDiagnosticAuto extends AbstractOpMode {
    Localizer localizer;
    MecanumDriveTrain drive;
    private final double POWER = 0.25;
    @Override
    protected void onInitialize() {
        drive = new MecanumDriveTrain(hardwareMap);
        localizer = new Localizer(hardwareMap, new Point(0,0), 0);
    }

    @Override
    protected void onStart() {
        while(opModeIsActive()){
            drive.setPower(POWER, POWER, POWER, POWER);
            Debug.log(localizer.getCurrentPosition());
            Debug.log(localizer.getGlobalRads());

        }
    }

    @Override
    protected void onStop() {

    }
}
