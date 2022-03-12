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

  public Robot()
  {
    // Set up our custom logger.
    try
    {
      Logging.CustomLogger.setup();
    }
    catch (Throwable e) 
    { 
      Logging.logException(e);
    }

  }

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

    chooser.addOption("Three Ball Auto", Constants.THREE_BALL_AUTO);
    chooser.setDefaultOption("Two Ball Auto", Constants.TWO_BALL_AUTO);
    chooser.addOption("One Ball Auto", Constants.ONE_BALL_AUTO);
    chooser.addOption("Zero Ball Auto", Constants.ZERO_BALL_AUTO);
    chooser.addOption("Do Nothing", Constants.DO_NOTHING);

    SmartDashboard.putNumber("shooterP", Constants.shooterP);
    SmartDashboard.putNumber("shooterI", Constants.shooterI);
    SmartDashboard.putNumber("shooterD", Constants.shooterD);
    
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
    //drive.drive(leftPilotX, leftPilotY);
    // A kinder, gentler joystick
    //drive.drive(scaleJoystickAxis(leftPilotX), leftPilotY);

    if (controller.getButton(Constants.PILOT, ButtonMap.turnTo))
      drive.driveTo(40, 5.0, true); 
    //drive.turnTo(180.0, 5.0);
    else if (controller.getButton(Constants.PILOT, ButtonMap.turnTo1))
      drive.driveTo(60, 5.0, false);
    //drive.turnTo(-85.0, 5.0);
    else
      drive.resetPid();
    
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
      int window;
      double limelightWindow;

      if (shooter.getDown())
      {
        if (controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
          {
            speed = Constants.farSpeed;
            window = Constants.farSpeedWindow;
            limelightWindow = Constants.farLimelightWindow;
          }
        else if (controller.getButton(Constants.PILOT, ButtonMap.autoShootMid))
          { 
            speed = Constants.midSpeed;
            window = Constants.midSpeedWindow;
            limelightWindow = Constants.midLimelightWindow;
          }
        else
        {
          speed = Constants.closeSpeed;
          window = Constants.closeSpeedWindow;
          limelightWindow = Constants.closeLimelightWindow;
        }
      }
      else
      {
        speed = Constants.upSpeed;
        window = Constants.upSpeedWindow;
        limelightWindow = Constants.upLimelightWindow;
      }

      boolean ret1 = shooter.shoot(speed, window);
      int ret2 = drive.centerToTarget(3.0, limelightWindow);
      System.out.printf("%b %d\n", ret1, ret2);

      SmartDashboard.putBoolean("ret1 (shoot)", ret1);
      SmartDashboard.putNumber("ret2 (centerToTarget)", ret2);

      if (ret1 && ret2 != 1)
        intake.indexerShoot();
      else
        intake.autoIndex();
    }
    else
    {
      drive.resetCenter();
      drive.drive(scaleJoystickAxis(leftPilotX), leftPilotY);
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
          shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow);
        else
          shooter.shoot(Constants.upSpeed, Constants.upSpeedWindow);
      }
      else if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeedMid))
      {
        if (shooter.getDown())
          shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
        else
          shooter.shoot(Constants.upSpeed, Constants.upSpeedWindow);
      }
      else if (controller.getButton(Constants.COPILOT, ButtonMap.shooterSpeedFar))
      {      
        if (shooter.getDown())
          shooter.shoot(Constants.farSpeed, Constants.farSpeedWindow);
        else
          shooter.shoot(Constants.upSpeed, Constants.upSpeedWindow);
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
    intake.autoIndex();
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
    SmartDashboard.putNumber("centerFailTimer", drive.centerFailTimer.get());
		SmartDashboard.putNumber("CenterIntervalTimer", drive.centerIntervalTimer.get());
    SmartDashboard.putBoolean("centerInitialized", drive.centerInitialized);
    SmartDashboard.putNumber("Camera has target:", Limelight.getValidTargets());
    SmartDashboard.putNumber("Target X (horiz) offset:", Limelight.getTargetAngleXOffset());
    SmartDashboard.putNumber("Target Y offset:", Limelight.getTargetAngleYOffset());
    SmartDashboard.putNumber("Left Gyro", drive.getImuYaw(false));
    SmartDashboard.putNumber("Right Gyro", drive.getImuYaw(true));
    SmartDashboard.putNumber("Encoder", drive.getRightEncoderPosition());
    SmartDashboard.putBoolean("Bottom Photoeye:", intake.getBottomEye());
    SmartDashboard.putBoolean("Top Photoeye:", intake.getTopEye());
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
      ret = drive.driveTo(40.0, 5.0, true);
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
      if (shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow))
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
      ret = drive.driveTo(90.0, 5.0, false); // may not be the correct distance
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
      ret = drive.centerToTarget(2.0, Constants.midLimelightWindow);
      if (ret == 0)
      {
        state++;
      }
      else if (ret == -1)
      {
        state++;
      }
      break;
    case 8:  
      autoTimer.start();
      if (shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow))
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
      ret = drive.driveTo(40.0, 5.0, true);
      intake.intakeIn();
      intake.intakeDown();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
        //System.out.println("successful drive");
      }
      else if (ret == -1)
        state = -1;
      break;
    case 2:
    SmartDashboard.putNumber("autoGyro", drive.getImuYaw(false));
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(180.0, 5.0);
      if (ret == 0)
      {
        //System.out.println("successful turn");
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
      }
      else if (ret == -1){
        //System.out.println("unsuccessful turn");
        drive.stop();
        autoTimer.stop();
        autoTimer.reset();
        state++;
        //state = -1;
      }
        //robot screwed up during comp. This ensures it shoots even if the robot timesout on turnTo
        //drive.stop();
        ///autoTimer.stop();
        //autoTimer.reset();
        //state++;
      break;
    case 3:
      ret = drive.driveTo(60.0, 5.0, false);
      shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow);
      intake.intakeUp();
      if (ret == 0) {
        drive.stop();
        intake.stopIntake();
        state++;
        //System.out.println("successful drive");
      }
      else if (ret == -1)
        state = -1;
      break;
    case 4:
      autoTimer.start();
      if (shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(2.5))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        drive.resetPid();
        state++;
      }
      break;
    case 5:
      ret = drive.driveTo(-75.0, 1.5, false);
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
      if (shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow))
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
      ret = drive.driveTo(-79.0, 3.0, false);
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
      ret = drive.driveTo(50.0, 5.0, true);
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

  // Scale the joystick value to mitigate oversteering
  private double scaleJoystickAxis(double input)
  {
    double scale = 0.7; // 0.7
    double output;

    // Start with a simple linear function for now
    // Might try a more advanced function like a sigmoid
    // later
    output = input * scale;

    // Someone on CD used a cube function, which is cool
    // Result is + for + numbers and - for - numbers
    //output = Math.pow(input, 3);

    return output;
  }

}
