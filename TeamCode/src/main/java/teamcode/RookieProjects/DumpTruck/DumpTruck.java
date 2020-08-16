package teamcode.RookieProjects.DumpTruck;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

public class DumpTruck {

    /*
    2 intake motors
    1 servo
    1 touch sensor
     */
    DcMotor leftIntake, rightIntake;
    Servo dumpTruck;
    TouchSensor intakeSensor;
    private final double DUMP_DOWN_POSITION = 0;
    private final double DUMP_UP_POSITION = 0.25;
    private DumpTruckState dumpState;

    private enum DumpTruckState{
        DUMP_TRUCK_DOWN, DUMP_TRUCK_UP;
    }

    public DumpTruck(HardwareMap hardwareMap){
        leftIntake = hardwareMap.dcMotor.get("leftIntake");
        rightIntake = hardwareMap.dcMotor.get("rightIntake");
        dumpTruck = hardwareMap.servo.get("dumpTruck");
        intakeSensor = hardwareMap.touchSensor.get("intakeSensor");
        dumpState = DumpTruckState.DUMP_TRUCK_DOWN;
        resetHardware();
    }

    private void resetHardware() {
        leftIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        dumpTruck.setPosition(DUMP_DOWN_POSITION);
    }




    public void intake(double power){
        leftIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftIntake.setPower(power);
        rightIntake.setPower(power);
        double previousEncoderValueLeft = leftIntake.getCurrentPosition();
        double previousEncoderValueRight = rightIntake.getCurrentPosition();

        while(!intakeSensor.isPressed()){
            double currentEncoderValueLeft = leftIntake.getCurrentPosition();
            double currentEncoderValueRight = rightIntake.getCurrentPosition();
            double deltaLeft = currentEncoderValueLeft - previousEncoderValueLeft;
            double deltaRight = currentEncoderValueRight - previousEncoderValueRight;
            if((leftIntake.getPower() * deltaLeft) / (deltaRight * rightIntake.getPower())<= 1.0){
                double leftMotorPower = (deltaLeft / deltaRight) * power;
                leftIntake.setPower(leftMotorPower);
            }else{
                double rightMotorPower = (deltaRight / deltaLeft) * power;
                leftIntake.setPower(rightMotorPower);
            }
            previousEncoderValueLeft = currentEncoderValueLeft;
            previousEncoderValueRight = currentEncoderValueRight;
        }
        leftIntake.setPower(0);
        rightIntake.setPower(0);
    }




    public void intakeSimple(double power){
        leftIntake.setPower(power);
        rightIntake.setPower(power);
    }

    public void adjustDumpValues(){
        if(dumpState == DumpTruckState.DUMP_TRUCK_DOWN){
            dumpState = DumpTruckState.DUMP_TRUCK_UP;
            dumpTruck.setPosition(DUMP_UP_POSITION);
        }else{
            dumpState = DumpTruckState.DUMP_TRUCK_DOWN;
            dumpTruck.setPosition(DUMP_DOWN_POSITION);
        }
    }


}
