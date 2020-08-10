package teamcode.RookieProjects;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.common.Vector2D;

public class SensorBotDriveTrain {

    DcMotor fl, fr, bl, br;

    public SensorBotDriveTrain(HardwareMap hardwareMap) {
        fl = hardwareMap.dcMotor.get("FrontLeftDrive");
        fr = hardwareMap.dcMotor.get("FrontRightDrive");
        bl = hardwareMap.dcMotor.get("BackLeftDrive");
        br = hardwareMap.dcMotor.get("BackRightDrive");
    }

    public void continuous(Vector2D velocity, double turnSpeed) {
        Vector2D velocity0 = new Vector2D(velocity.getX(), -velocity.getY());
        double direction = velocity0.getDirection();

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
