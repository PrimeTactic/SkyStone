package teamcode.RookieProjects;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Timer;
import java.util.TimerTask;

public class SensorBot {
    /*
    hardwareMap:
    4 drive motors mecanum drivetrain
    Servo flap (0 = up 0.5 = down)
    color sensor (blue)
    ok so set up 3 classes, opMode (TeleOp), DriveTrain(Extendibility) and use this to handle moving components
     */

    Servo flap;
    ColorSensor sensor;
    private final double FLAP_DOWN_POSITION = 0.5;
    private final double FLAP_UP_POSITION = 0;
    FlapState currentState;


    enum FlapState{
        FLAP_UP,
        FLAP_DOWN;
    }



    public SensorBot(HardwareMap hardwareMap) {
        flap = hardwareMap.servo.get("flap");
        sensor = hardwareMap.colorSensor.get("groundSensor");
        resetServos();
    }

    private void resetServos(){
        flap.setPosition(FLAP_UP_POSITION);
        currentState = FlapState.FLAP_UP;
        canAdjustFlap = true;
    }

    public int[] updateColorSensorValues() {
        int red = sensor.red();
        int green = sensor.green();
        int blue = sensor.blue();
        return new int[]{red, green, blue};
    }

    public int[] updateColorSensorValuesCOMP(){
        return new int[]{sensor.red(), sensor.green(), sensor.blue()};
    }

    boolean canAdjustFlap;


    public void adjustFlap() {
        if(canAdjustFlap && currentState == FlapState.FLAP_UP){
            flap.setPosition(FLAP_DOWN_POSITION);
        }else{
            flap.setPosition(FLAP_UP_POSITION);
        }
        canAdjustFlap = false;
        TimerTask canAdjust = new TimerTask() {
            @Override
            public void run() {
                canAdjustFlap = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(canAdjust, 500);

    }

}
