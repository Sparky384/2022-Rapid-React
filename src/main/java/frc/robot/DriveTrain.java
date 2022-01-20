package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

public class DriveTrain 
{
    WPI_TalonFX frontleft;
    WPI_TalonFX frontright;
    WPI_TalonFX rearleft;
    WPI_TalonFX rearright;
    MotorControllerGroup left;
    MotorControllerGroup right;
    DifferentialDrive wheels;

    DriveTrain()
    {
        frontleft = new WPI_TalonFX(1);
        frontright = new WPI_TalonFX(14);
        rearleft = new WPI_TalonFX(16);
        rearright = new WPI_TalonFX(15);
        left = new MotorControllerGroup(frontleft, rearleft);
        right = new MotorControllerGroup(frontright, rearright);
        wheels = new DifferentialDrive(left, right);

        right.setInverted(true);
    }

    public void drive(double speed, double turn)
    {
        wheels.arcadeDrive(speed, turn);
    }
}
