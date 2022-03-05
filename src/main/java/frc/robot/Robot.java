// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import java.io.File;

import javax.lang.model.util.ElementScanner6;

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
  private Timer autoTimer;
  private Compressor compressor;
  private Climber climb;

  @Override
  public void robotInit() {
    // check if the robot is the final or prototype
    /*File f = new File("/home/admin/proto");
    if (f.exists())
    {
      SmartDashboard.putString("profile", "proto");
      new Constants(Constants.proto); // sets the port numbers
    }
    else
    {
      SmartDashboard.putString("profile", "sparky");
      new Constants(Constants.sparky); // sets the port numbers
    }*/
    new Constants(Constants.sparky);
    SmartDashboard.putNumber("Shooter Percent", 0.0);

    drive = new DriveTrain();
    intake = new Intake();
    controller = new Controller();
    shooter = new Shooter();  
    chooser = new SendableChooser<Integer>();
    climb = new Climber();

    chooser.setDefaultOption("Two Ball Auto", Constants.TWO_BALL_AUTO);
    chooser.addOption("One Ball Auto", Constants.ONE_BALL_AUTO);
    chooser.addOption("Zero Ball Auto", Constants.ZERO_BALL_AUTO);
    chooser.addOption("Do Nothing", Constants.DO_NOTHING);
    
    drive.imuZeroYaw();
    drive.initializeEncoders();
    
    SmartDashboard.putData("Autonomous Chooser", chooser);

    autoTimer = new Timer();

    compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
    compressor.enableDigital();
  }
  
  public void teleopInit()
  {
    drive.initializeEncoders();
    drive.imuZeroYaw();
  }

  @Override
  public void teleopPeriodic() 
  {
    // pilot commands
    double leftPilotY = controller.getLeftY(Constants.PILOT);
    double leftPilotX = -controller.getLeftX(Constants.PILOT);
    double rightPilotY = controller.getRightY(Constants.PILOT);
    drive.drive(leftPilotX, leftPilotY);

    if (controller.getButton(Constants.PILOT, ButtonMap.climberSafety))
    {
      // climber control goes on right stick
      climb.move(rightPilotY);
    }
    else
      climb.move(0);

    if (controller.getButton(Constants.PILOT, ButtonMap.intakeOut))
    {
      intake.intakeDown();
      intake.intakeIn();
    }
    else
    {
      intake.intakeUp();
      intake.stopIntake();
    }

    if (controller.getButton(Constants.PILOT, ButtonMap.autoShootMid) || 
      controller.getButton(Constants.PILOT, ButtonMap.autoShootFar) ||
      controller.getButton(Constants.PILOT, ButtonMap.autoShootClose))
    {
      double speed;
      if (shooter.getDown())
      {
        if (controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
          speed = Constants.farSpeed;
        else if (controller.getButton(Constants.PILOT, ButtonMap.autoShootMid))
          speed = Constants.midSpeed;
        else
          speed = Constants.closeSpeed;
      }
      else
        speed = Constants.upSpeed;

      if (shooter.shoot(speed) && drive.centerToTarget(15.0) == 1)
        intake.indexerShoot();
      else
        intake.autoIndex();
    }
    else
    {
      // give control of shooter/indexer to copilot if not auto shooting
      if (controller.getButton(Constants.COPILOT, ButtonMap.indexerOut))
        intake.indexerOut();
      else if (controller.getButton(Constants.COPILOT, ButtonMap.indexerIn))
        intake.indexerShoot();
      else 
        intake.autoIndex();

      if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeedClose))
      {
        if (shooter.getDown())
          shooter.shoot(Constants.closeSpeed);
        else
          shooter.shoot(Constants.upSpeed);
      }
      else if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeedMid))
      {
        if (shooter.getDown())
          shooter.shoot(Constants.midSpeed);
        else
          shooter.shoot(Constants.upSpeed);
      }
      else if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeedFar))
      {      
        if (shooter.getDown())
          shooter.shoot(Constants.farSpeed);
        else
          shooter.shoot(Constants.upSpeed);
      }
      else
        shooter.shootStop();
    }

    // copilot commands
    if (controller.getButton(Constants.COPILOT, ButtonMap.shooterUp))
      shooter.shooterUp();
    if (controller.getButton(Constants.COPILOT, ButtonMap.shooterDown))
      shooter.shooterDown();

    dashboardOutput();
  }

  @Override
  public void autonomousInit() 
  {
    state = 0;
    drive.imuZeroYaw();
    drive.initializeEncoders();
  }

   public void autonomousPeriodic() 
   {
    switch(chooser.getSelected()) 
    {
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
      // if do nothing is selected nothing will execute and robot will sit still
    }
  } 

  private void dashboardOutput() 
  {
    SmartDashboard.putNumber("Camera has target:", Limelight.getValidTargets());
    SmartDashboard.putNumber("Target X (horiz) offset:", Limelight.getTargetAngleXOffset());
    SmartDashboard.putNumber("Target Y offset:", Limelight.getTargetAngleYOffset());
    SmartDashboard.putNumber("Gyro", drive.getImuYaw(false));
    SmartDashboard.putNumber("Encoder", drive.getRightEncoderPosition());
  }

  private void threeBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      autoTimer.start();
      intake.intakeDown();
      intake.intakeIn();
      if (autoTimer.hasElapsed(1.0));
        state++;
    case 1:
      ret = drive.driveTo(40.0, 5.0);
      intake.intakeIn();
      intake.intakeDown();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
        autoTimer.stop();
        autoTimer.reset();
      }
      else if (ret == -1)
        state = -1;
      break;
    case 2:
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(180.0, 5.0);
      if (ret == 0)
      {
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
        autoTimer.stop();
        autoTimer.reset();
      }
      else if (ret == -1)
        state = -1;
      break;
    case 3:
      autoTimer.start();
      if (shooter.shoot(Constants.midSpeed))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(4.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
        autoTimer.stop();
        autoTimer.reset();
      }
      break;
    case 4:
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(-85.0, 5.0);
      if (ret == 0)
      {
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 5:
      ret = drive.driveTo(90.0, 5.0); // may not be the correct distance
      intake.intakeIn();
      intake.intakeDown();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
        autoTimer.stop();
        autoTimer.reset();
      }
      else if (ret == -1)
        state = -1;
      break;
    case 6:
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(95.0, 5.0);
      if (ret == 0)
      {
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 7:  
      autoTimer.start();
      if (shooter.shoot(Constants.midSpeed))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(4.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
      }
      break;
    default:
      stopEverything();
      break;
    }
  }

  private void twoBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      autoTimer.start();
      intake.intakeDown();
      intake.intakeIn();
      if (autoTimer.hasElapsed(1.0));
        state++;
    case 1:
      ret = drive.driveTo(40.0, 5.0);
      intake.intakeIn();
      intake.intakeDown();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 2:
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(180.0, 5.0);
      if (ret == 0)
      {
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    case 3:
      autoTimer.start();
      if (shooter.shoot(Constants.midSpeed))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(4.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        drive.resetPid();
        state++;
      }
      break;
    case 4:
      ret = drive.driveTo(-15.0, 5.0);
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
      }
      else if (ret == -1)
        state = -1;
      break;
    default:
      stopEverything();  
      break;
    }
  }

  private void oneBallAuto()
  {
    int ret;
    switch (state)
    {
    case 0:
      autoTimer.start();
      if (shooter.shoot(Constants.closeSpeed))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(3.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        state++;
      }
      break;
    case 1:
      ret = drive.driveTo(-79.0, 3.0);
      if (ret == 0)
        state++;
      else if (ret == -1)
        state = -1;
      break;
    default:
      stopEverything();
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
      stopEverything();
      break;
    }
  }

  private void stopEverything()
  {
    drive.stop();
    intake.stopIndex();
    intake.stopIntake();
    shooter.shootStop();
  }

}
