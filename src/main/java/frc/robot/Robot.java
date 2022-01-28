// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;


/**
 * This is a demo program showing the use of the DifferentialDrive class. Runs the motors with
 * arcade steering.
 */
public class Robot extends TimedRobot {
  DriveTrain drive = new DriveTrain();
  Intake intake = new Intake();
  Controller controller = new Controller();
  Shooter shooter = new Shooter();
  

  // TalonSRX frontleft = new TalonSRX(1);
  // private final PWMSparkMax m_leftMotor = new PWMSparkMax(0);
  // private final PWMSparkMax m_rightMotor = new PWMSparkMax(1);
  // private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftMotor, m_rightMotor);
  // private final Joystick m_stick = new Joystick(0);

  @Override
  public void robotInit() {
   //SmartDashboard.putNumber("Shooterspeed:",controller.getRightY());
   //SmartDashboard.putNumber("Motorspeed:",controller.getLeftY());
   //SmartDashboard.putBoolean("BottomPhotoEyeBlocked:", false);
    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    // m_rightMotor.setInverted(true);
  }

  @Override
  public void teleopPeriodic() {
  
    // Drive with arcade drive.
    // That means that the Y axis drives forward
    // and backward, and the X turns left and right.
    // m_robotDrive.arcadeDrive(-m_stick.getY(), m_stick.getX());
    SmartDashboard.putNumber("Rightjoystick:",controller.getRightY());
    SmartDashboard.putNumber("Leftjoystick:",controller.getLeftY());
    drive.drive();
    intake.innerIntake();
    if (controller.getXButton())
      intake.intakeIn();
    if (controller.getAButton())
      intake.intakeOut();
    else if (!controller.getXButton() && !controller.getAButton())
      intake.stopIntake();
    //if (controller.getBButton() ) //|| photoEye.get())
      //intake.stopIntake();
    shooter.shoot();
    //System.out.println(controller.getXButton());
    
  } 
  
  

  
}
