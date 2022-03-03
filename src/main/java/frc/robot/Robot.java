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
  private SendableChooser<Double> downSpeedChooser;
  private SendableChooser<Double> upSpeedChooser;
  private int ret1;
  private int ret2;
  private Timer autoTimer;
  private boolean hasStartedAutoTimer;
  private Compressor compressor;

  @Override
  public void robotInit() {
    SmartDashboard.putNumber("speed", 0.0);
    File f = new File("/home/admin/proto");
    if (f.exists())
    {
      SmartDashboard.putString("profile", "proto");
      new Constants(Constants.proto); // sets the port numbers
    }
    else
    {
      SmartDashboard.putString("profile", "sparky");
      new Constants(Constants.sparky); // sets the port numbers
    }
    SmartDashboard.putNumber("Drive P", Constants.driveP);
    SmartDashboard.putNumber("Drive I", Constants.driveI);
    SmartDashboard.putNumber("Drive D", Constants.driveD);
    SmartDashboard.putNumber("Drive I Up", Constants.driveIZoneUpper);
    SmartDashboard.putNumber("Drive I Lo", Constants.driveIZoneLower);

    SmartDashboard.putNumber("Turn P", Constants.turnP);
    SmartDashboard.putNumber("Turn I", Constants.turnI);
    SmartDashboard.putNumber("Turn D", Constants.turnD);
    SmartDashboard.putNumber("Turn I Up", Constants.turnIZoneUpper);
    SmartDashboard.putNumber("Turn I Lo", Constants.turnIZoneLower);

    SmartDashboard.putNumber("Shoot P", Constants.shooterP);
    SmartDashboard.putNumber("Shoot I", Constants.shooterI);
    SmartDashboard.putNumber("Shoot D", Constants.shooterD);
    SmartDashboard.putNumber("Shoot Pin", 0.0);
    SmartDashboard.putNumber("Shooter Percent", 0.0);

    drive = new DriveTrain();
    intake = new Intake();
    controller = new Controller();
    shooter = new Shooter();  
    chooser = new SendableChooser<Integer>();
    downSpeedChooser = new SendableChooser<Double>();
    upSpeedChooser = new SendableChooser<Double>();

    downSpeedChooser.addOption("Down Speed", 1000.0);
    downSpeedChooser.setDefaultOption("1000", 1000.0);
    downSpeedChooser.addOption("1300", 1300.0);
    downSpeedChooser.addOption("1600", 1600.0);
    downSpeedChooser.addOption("1900", 1900.0);
    downSpeedChooser.addOption("2200", 2200.0);
    downSpeedChooser.addOption("2500", 2500.0);
    downSpeedChooser.addOption("2800", 2800.0);
    downSpeedChooser.addOption("3100", 3100.0);
    downSpeedChooser.addOption("3400", 3400.0);
    downSpeedChooser.addOption("3700", 3700.0);
    downSpeedChooser.addOption("4000", 4000.0);

    upSpeedChooser.addOption("Up Speed", 1000.0);
    upSpeedChooser.setDefaultOption("1000", 1000.0);
    upSpeedChooser.addOption("1300", 1300.0);
    upSpeedChooser.addOption("1600", 1600.0);
    upSpeedChooser.addOption("1900", 1900.0);
    upSpeedChooser.addOption("2200", 2200.0);
    upSpeedChooser.addOption("2500", 2500.0);
    upSpeedChooser.addOption("2800", 2800.0);
    upSpeedChooser.addOption("3100", 3100.0);
    upSpeedChooser.addOption("3400", 3400.0);
    upSpeedChooser.addOption("3700", 3700.0);
    upSpeedChooser.addOption("4000", 4000.0);

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
    SmartDashboard.putData("Down Shooter Speed", downSpeedChooser);
    SmartDashboard.putData("Up Shooter Speed", upSpeedChooser);
    ret1 = 0;
    ret2 = 0;

    autoTimer = new Timer();
    hasStartedAutoTimer = false;

    compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
    compressor.enableDigital();
    //compressor.disable();
  }
  
  public void teleopInit()
  {
    drive.initializeEncoders();
  }

  @Override
  public void teleopPeriodic() 
  {
    // pilot commands
    double pilotY = controller.getLeftY(Constants.PILOT);
    double pilotX = -controller.getLeftX(Constants.PILOT);
    drive.drive(pilotX, pilotY);

    if (controller.getButton(Constants.PILOT, ButtonMap.climberSafety))
    {
      // climber control goes on right stick
    }

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

    if (controller.getButton(Constants.PILOT, ButtonMap.autoShoot))
    {
      double speed = 0.0;
      if (shooter.getDown())
        speed = downSpeedChooser.getSelected();
      else
        speed = upSpeedChooser.getSelected();

      //shooter.testShoot(0.5);
      if (shooter.testShoot(SmartDashboard.getNumber("Shooter Percent", 0.5)))// && drive.centerToTarget(15.0) == 1)
        intake.indexerShoot();
      else
        intake.stopIndex();
        //intake.autoIndex();
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

      if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeed1))
        shooter.testShoot(0.4);
      else if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeed2))
        shooter.testShoot(0.5);
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
      if (shooter.shoot(1000.0))
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
      if (shooter.shoot(1000.0))
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
      if (shooter.shoot(1000.0)) 
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
      if (shooter.shoot(1000.0)) 
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
