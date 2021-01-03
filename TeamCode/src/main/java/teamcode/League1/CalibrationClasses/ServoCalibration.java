package teamcode.League1.CalibrationClasses;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;

@TeleOp(name="Servo Calibration")
public class ServoCalibration extends AbstractOpMode {
    Servo index;
    @Override
    protected void onInitialize() {
        index = hardwareMap.servo.get("Indexer");

    }

    @Override
    protected void onStart() {

        index.setPosition(0.65);
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        index.setPosition(1.0);
        while(opModeIsActive());


    }

    @Override
    protected void onStop() {

    }
}
