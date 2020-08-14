package teamcode.common;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import teamcode.common.PurePursuit.MovementVars;

public class MecanumDriveTrain {

    /*
    This has most of the relevant information regarding a 4 wheel Mechanum DriveTrain,
    which is the most used DriveTrain in FTC
     */

    private DcMotor fl, fr, bl, br;

    public MecanumDriveTrain(HardwareMap hardwareMap){
        fl = hardwareMap.dcMotor.get("FrontLeftDrive");
        fr = hardwareMap.dcMotor.get("FrontRightDrive");
        bl = hardwareMap.dcMotor.get("BackLeftDrive");
        br = hardwareMap.dcMotor.get("BackRightDrive");
    }

    /*
    gets the robot driving in a specified direction
     */
    public void setPower(Vector2D velocity, double turnValue){

        double direction = velocity.getDirection();

        double maxPow = Math.sin(Math.PI / 4);
        double power = velocity.magnitude() / maxPow;

        double angle = direction + 3 * Math.PI / 4;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        setPower((power * sin - MovementVars.movementTurn), (power * cos + MovementVars.movementTurn),
                (power * cos - MovementVars.movementTurn), (power * sin + MovementVars.movementTurn));
    }

    public void setPower(double flPow, double frPow, double blPow, double brPow) {
        fl.setPower(flPow);
        fr.setPower(frPow);
        bl.setPower(blPow);
        br.setPower(brPow);
    }
}
