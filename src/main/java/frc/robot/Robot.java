// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
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
  private Timer autoTimer;
  private boolean hasStartedAutoTimer;
  private Compressor compressor;

  @Override
  public void robotInit() {
    drive = new DriveTrain();
    intake = new Intake();
    controller = new Controller();
    shooter = new Shooter();
    chooser = new SendableChooser<Integer>();

    chooser.setDefaultOption("Turn and Drive", Constants.TURN_AND_DRIVE);
    chooser.addOption("Drive and Turn", Constants.DRIVE_AND_TURN);
    chooser.addOption("Three Ball Auto", Constants.THREE_BALL_AUTO);
    chooser.addOption("Two Ball Auto", Constants.TWO_BALL_AUTO);
    chooser.addOption("One Ball Auto", Constants.ONE_BALL_AUTO);
    chooser.addOption("Zero Ball Auto", Constants.ZERO_BALL_AUTO);
    chooser.addOption("Do Nothing", Constants.DO_NOTHING);
    chooser.addOption("Turn", Constants.TURN);

    
    
    drive.imuZeroYaw();
    shooter.resetTurnEncoder();
    
    SmartDashboard.putData("Autonomous Chooser", chooser);
    System.out.println("look at me im robot inited, i inited");
    ret1 = 0;
    ret2 = 0;

    autoTimer = new Timer();
    hasStartedAutoTimer = false;

    compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
    compressor.enableDigital();
    compressor.disable();
  }
  
  public void teleopInit()
  {
    drive.initializeEncoders();
  }

  @Override
  public void teleopPeriodic() 
  {
    double pilotY = 0.8*controller.getLeftY(Constants.PILOT);
    double pilotX = -0.8 * controller.getLeftX(Constants.PILOT);
    drive.drive(pilotX, pilotY);
    SmartDashboard.putNumber("rightY", controller.getRightY(Constants.PILOT));

    if (controller.getLeftBumper(Constants.PILOT))
      shooter.toggle();
    if (controller.getBButton(Constants.PILOT))
      intake.toggle();
    if (controller.getXButton(Constants.PILOT))
      intake.intakeIn();
    if (controller.getAButton(Constants.PILOT))
      intake.intakeOut();
    else if (!controller.getXButton(Constants.PILOT) && !controller.getAButton(Constants.PILOT))
      intake.stopIntake();
    if(controller.getLeftTrigger(Constants.PILOT) || ret2 == 1)
     ret2 = drive.driveTo(60, 5);
    
    /*if (controller.getYButton(Constants.PILOT))
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
      intake.autoIndex();
    }
    */
    dashboardOutput();
    
    SmartDashboard.putNumber("ret1", ret1);
    if (controller.getYButton(Constants.PILOT) || ret1 == 1)
    {
      ret1 = drive.centerToTarget(10.0);
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
      case Constants.THREE_BALL_AUTO:
        threeBallAuto();
        break;
      case Constants.TWO_BALL_AUTO:
        twoBallAuto();
        break;
      case Constants.ONE_BALL_AUTO:
        oneBallAuto();
        break;
      case Constants.ZERO_BALL_AUTO:
        zeroBallAuto();
        break;
      case Constants.TURN:
        SmartDashboard.putNumber("Gyro", drive.getImuYaw(true)); 
        turn();
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
  
  public void turn() 
  {
    int ret;
    switch (state) 
    {
      case 0:  
        ret = drive.turnTo(180, 10);
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
    SmartDashboard.putNumber("Gyro", drive.getImuYaw(true));
    SmartDashboard.putNumber("Encoder", drive.getRightEncoderPosition());
  }

  private void threeBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      ret = drive.driveTo(63.0, 5.0);
      intake.intakeIn();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 1:
      ret = drive.turnTo(-90.0, 8.0); //TODO SHOULD BE NEAR 180 RETUNE PID WITH ONE FINAL
      if (ret == 0)
      {
        drive.stop();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 2:
      if (!hasStartedAutoTimer)
        autoTimer.start();
      if (shooter.shoot())
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(6.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        //state++;
        state = 4;
      }
      break;
   /*  case 3:
      ret = drive.turnTo(0.0, 5.0); //TODO MAKE 95 DEGREES AFTER FIXING 180 ISSUE
      if (ret == 0)
      {
        drive.stop();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break; */
    case 4: 
      ret = drive.driveTo(93.0, 5.0);
      intake.intakeIn();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 5:
      ret = drive.turnTo(-90.0, 5.0);
      if (ret == 0)
      {
        drive.stop();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 6:
      if (!hasStartedAutoTimer)
      autoTimer.start();
      if (shooter.shoot())
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(6.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
      }
      break;
    default:
      drive.stop();
      intake.stopIntake();
      shooter.shootStop();
      intake.stopIndex();
      break;
    }
  }

  private void twoBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      ret = drive.driveTo(63.0, 5.0);
      intake.intakeIn();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 1:
      ret = drive.turnTo(90.0, 5.0); //TODO SHOULD BE NEAR 180 RETUNE PID WITH ONE FINAL
      if (ret == 0)
      {
        drive.stop();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 2:
      if (!hasStartedAutoTimer)
        autoTimer.start();
      if (shooter.shoot()) 
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(6.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
      }
      break;
    default:
      drive.stop();
      intake.stopIntake();
      intake.stopIndex();
      shooter.shootStop();
      break;
    }
  }

  private void oneBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      if (!hasStartedAutoTimer)
        autoTimer.start();
      if (shooter.shoot()) 
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(6.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
      }
      break;
    case 1:
      ret = drive.driveTo(-97.0, 5.0);
      if (ret == 0)
        state++;
      else if (ret == -1)
        state = -1;
      break;
    default:
      drive.stop();
      shooter.shootStop();
      intake.stopIndex();
      break;
    }
  }

  private void zeroBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      ret = drive.driveTo(50.0, 5.0);
      if (ret == 0)
        state++;
      else if (ret == -1)
        state = -1;
      break;
    default:
      drive.stop();
      break;
    }
  }

}
