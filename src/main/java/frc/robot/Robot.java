// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

public class Robot extends TimedRobot 
{
  private DriveTrain drive;
  private Intake intake;
  private Controller controller;
  private Shooter shooter;
  private int state;
  private SendableChooser<Integer> chooser;

  @Override
  public void robotInit() {
    drive = new DriveTrain();
    intake = new Intake();
    controller = new Controller();
    shooter = new Shooter();
    chooser = new SendableChooser<Integer>();

    chooser.setDefaultOption("Turn and Drive", Constants.TURN_AND_DRIVE);
    chooser.addOption("Drive and Turn", Constants.DRIVE_AND_TURN);
    
    drive.imuZeroYaw();
    shooter.resetTurnEncoder();
    
    SmartDashboard.putData("Autonomous Chooser", chooser);
  }

  @Override
  public void teleopPeriodic() 
  {
    SmartDashboard.putNumber("Yaw", drive.getImuYaw());
    drive.drive();
    
    if (controller.getLeftBumper())
      shooter.shooterTurnLeft();
    else if(controller.getRightBumper())
      shooter.shooterTurnRight();
    else if(controller.getBButton())
      shooter.shooterTurnStraight();
    else
      shooter.stopTurn();

    if (controller.getXButton())
      intake.intakeIn();
    if (controller.getAButton())
      intake.intakeOut();
    else if (!controller.getXButton() && !controller.getAButton())
      intake.stopIntake();

    if (controller.getYButton())
    {
      if (shooter.shoot())
      {
        intake.checkEyes();  
        intake.indexerShoot();
      }
      else
        intake.autoIndex();
    }
    else
    {
      shooter.shootStop();
      intake.autoIndex();
    }
  }

  @Override
  public void autonomousInit() 
  {
    state = 0;
    drive.imuZeroYaw();
    shooter.resetTurnEncoder();
  }

   public void autonomousPeriodic() 
   {
    switch(chooser.getSelected()) 
    {
      case Constants.TURN_AND_DRIVE:
        turnAndDrive();
        break;
      case Constants.DRIVE_AND_TURN:
        driveAndTurn();
        break;
    }
  } 

  public void turnAndDrive() 
  {
    int ret;
    switch (state) 
    {
      case 0:  
        ret = drive.turnTo(45, 5);
        if (ret == 0 || ret == -1)
          state++;
        break;
      case 1:  
        ret = drive.driveTo(60, 5);
        if (ret == 0 || ret == -1)
          state++;
        break;
    }
  }
  
  public void driveAndTurn () 
  {
    int ret;
    switch (state) 
    {
      case 0:  
        ret = drive.driveTo(60, 5);
        if (ret == 0 || ret == -1)
          state++;
        break;
      case 1:
        ret = drive.turnTo(45, 5);
        if (ret == 0 || ret == -1)
          state++;
        break;
    }
  }
}
