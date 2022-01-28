package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.revrobotics.RelativeEncoder;

public class DriveTrain {
    WPI_TalonFX frontLeft = new WPI_TalonFX(1);
    WPI_TalonFX frontRight = new WPI_TalonFX(14);
    WPI_TalonFX backLeft = new WPI_TalonFX(16);
    WPI_TalonFX backRight = new WPI_TalonFX(15);
    //WPI_TalonFX frontLeft;
    //WPI_TalonFX frontRight;
    //WPI_TalonFX backLeft;
    //WPI_TalonFX backRight;
    MotorControllerGroup leftMotors = new MotorControllerGroup(frontLeft, backLeft);
    MotorControllerGroup rightMotors = new MotorControllerGroup(frontRight, backRight);
    Controller pilot = new Controller();
    DifferentialDrive difDrive = new DifferentialDrive(leftMotors, rightMotors);
    //motorvariable.congifSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10)

    DriveTrain() {
        //frontLeft = new WPI_TalonFX(1); - Had to define the Talons above with the motor type.
        //frontRight = new WPI_TalonFX(14); - Had to define the Talons above with the motor type.
        //backLeft = new WPI_TalonFX(16); - Had to define the Talons above with the motor type.
        //backRight = new WPI_TalonFX(15); - Had to define the Talons above with the motor type.
        //rightMotors.getInverted();

    }

    public void drive() {
        double pilotY = pilot.getLeftY();
        double pilotX = -1 * pilot.getLeftX();
        difDrive.arcadeDrive(pilotX, pilotY);
    }
}
