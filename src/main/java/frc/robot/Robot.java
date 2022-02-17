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
  private int ret1;
  private int ret2;

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
    System.out.println("look at me im robot inited, i inited");
    ret1 = 0;
    ret2 = 0;
  }

  @Override
  public void teleopPeriodic() 
  {
    double pilotY = 0.8*controller.getLeftY(Constants.PILOT);
    double pilotX = -0.8 * controller.getLeftX(Constants.PILOT);
    drive.drive(pilotX, pilotY);
    SmartDashboard.putNumber("rightY", controller.getRightY(Constants.PILOT));
    
    if (controller.getLeftBumper(Constants.PILOT))
      shooter.shooterTurnLeft();
    else if(controller.getRightBumper(Constants.PILOT))
      shooter.shooterTurnRight();
    else if(controller.getBButton(Constants.PILOT))
      shooter.shooterTurnStraight();
    else
      shooter.stopTurn();
    
    if (controller.getXButton(Constants.PILOT))
      intake.intakeIn();
    if (controller.getAButton(Constants.PILOT))
      intake.intakeOut();
    else if (!controller.getXButton(Constants.PILOT) && !controller.getAButton(Constants.PILOT))
      intake.stopIntake();
    if(controller.getLeftTrigger(Constants.PILOT) || ret2 == 1)
     ret2 = drive.driveTo(60, 5);
    
    if (controller.getYButton(Constants.PILOT))
    {
      if (shooter.shoot())
      {
        intake.indexerShoot();
      }
      else
        intake.autoIndex();
    }
    else
    {
      shooter.stickShoot(controller.getRightY(Constants.PILOT));
      //shooter.shootStop();
      intake.autoIndex();
    }
    dashboardOutput();
    
    SmartDashboard.putNumber("ret1", ret1);
    /*if (controller.getYButton(Constants.PILOT) || ret1 == 1)
    {
      ret1 = drive.centerToTarget(10.0);
    }
    */
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
      case Constants.DO_NOTHING:
      //Does Nothing
        break;
      case Constants.TEST:  

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

  private void dashboardOutput() 
  {
    SmartDashboard.putNumber("Camera has target:", Limelight.getValidTargets());
    SmartDashboard.putNumber("Target X (horiz) offset:", Limelight.getTargetAngleXOffset());
    SmartDashboard.putNumber("Target Y offset:", Limelight.getTargetAngleYOffset());
  }

}
