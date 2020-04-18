package teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.common.Vector2D;

class DriveSystemTutorial {
    private DcMotor fl, fr, bl, br;

    public DriveSystemTutorial(HardwareMap hardwareMap) {
        fl = hardwareMap.dcMotor.get("FrontLeftDrive");
        fr = hardwareMap.dcMotor.get("FrontRightDrive");
        bl = hardwareMap.dcMotor.get("BackLeftDrive");
        br = hardwareMap.dcMotor.get("BackRightDrive");

    }

    public void continous(Vector2D velocity, double turnSpeed){

        double direction = velocity.getDirection();

        double maxPow = Math.sin(Math.PI / 4);
        double power = velocity.magnitude() / maxPow;

        double angle = direction + 3 * Math.PI / 4;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        fl.setPower(power * sin - turnSpeed);
        fr.setPower(power * cos + turnSpeed);
        bl.setPower(power * cos - turnSpeed);
        br.setPower(power * sin + turnSpeed);
    }
}
