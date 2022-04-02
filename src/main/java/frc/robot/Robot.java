// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import java.io.File;
import java.io.IOException;

import javax.lang.model.util.ElementScanner6;

import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Joystick;
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
  private boolean runFirstCenter;

  // Limelight distance calculations
  LimelightData limelightData = new LimelightData();

  // Currently used for close shot only - what about mid and far shots?
  private boolean shooterAtSpeed;
  private boolean ret1 = false;

  private Timer rumbleTimer; 

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
    //SmartDashboard.putNumber("Shooter Percent", 0.0);

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
    rumbleTimer = new Timer();

    compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
    compressor.enableDigital();
    intake.unlockIndex();
    runFirstCenter = true;
  }
  
  public void teleopInit()
  {
    drive.initializeEncoders();
    drive.imuZeroYaw();
    intake.unlockIndex();
    Limelight.lightOff(Constants.GOAL);
    Limelight.lightOff(Constants.BALL);
  }

  @Override
  public void teleopPeriodic() 
  {
    // pilot commands
    double leftPilotY = controller.getLeftY(Constants.PILOT);
    double leftPilotX = -controller.getLeftX(Constants.PILOT);
    double rightPilotY = controller.getRightY(Constants.PILOT);

    // Track the number of balls in the intake - must run continuously
    //intake.updateIndexBallCount();

    // ***************************************************************
    // I guess this is for testing/tuning the turnTo PID
    if (controller.getButton(Constants.PILOT, ButtonMap.turnTo))
      drive.driveTo(40, 5.0, true); 
    //drive.turnTo(180.0, 5.0);
    else if (controller.getButton(Constants.PILOT, ButtonMap.turnTo1))
      drive.driveTo(60, 5.0, false);
    //drive.turnTo(-85.0, 5.0);
    else
      drive.resetPid();
    // ***************************************************************

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

    // Look and see if buttons for auto shoot and auto limelight centering
    // are pressed
    if (controller.getButton(Constants.PILOT, ButtonMap.autoShootMid) || 
      controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
    {
      double speed;
      int window;
      double limelightWindow;
      shooter.shooterUp();

      // Turn on the limelight
      Limelight.lightOn(Constants.GOAL);

      // A post shot is being made
      if (controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
      {
        speed = Constants.farSpeed;
        window = Constants.farSpeedWindow;
        limelightWindow = Constants.farLimelightWindow;
      }
      /*
      A mid shot is being made, so speed is a function of limelight, unless the
      camera doesn't target. If that is the case, then we'll load a midspeed
      value, which should be a halfway point best guess
      */
      else if (controller.getButton(Constants.PILOT, ButtonMap.autoShootMid))
      { 
        speed = Constants.midSpeed;
        window = Constants.midSpeedWindow;
        limelightWindow = Constants.midLimelightWindow;
      }
      else  // we require a default speed here, or compiler will complain
      {
        speed = Constants.midSpeed;
        window = Constants.midSpeedWindow;
        limelightWindow = Constants.midLimelightWindow;
      }

      /*
      Now center and take the shot

      If we get a success value from limelight centering, lock in calculated
      value of speed and pass it to shooter.shoot().  If not, we punt and pass the 
      midSpeed value
      */
      
      rumbleTimer.start();
      //if there are no valid targets, make the pilot controller rumble
      if((int) Limelight.getValidTargets(Constants.GOAL) == 0 && Limelight.isLimelightAlive() && rumbleTimer.hasElapsed(0.25))
      {
        controller.rumble(Constants.PILOT);
      }
      else
        controller.stopRumble(Constants.PILOT);

      int ret2 = 0;
      if (runFirstCenter)
        ret2 = drive.centerToTarget(3.0, limelightWindow, Constants.GOAL, -1);
      
      // Initialize with false until centering started
      //boolean ret1 = false;

      // Centering successful, do a calculated shot
      if(ret2 == 0) {
        if (runFirstCenter)
          drive.resetCenter();
        runFirstCenter = false;
        speed = limelightData.interpolate
          (limelightData.finalDistance, limelightData.rpm, Limelight.calculateDistance(Constants.GOAL));
          
          ret2 = drive.centerToTarget(3.0, limelightWindow, Constants.GOAL, (int) speed);
          // The interpolator blows up when the robot is too close (41" or less)
          if(speed < 1900.0) {
            speed = 1900;
          }

        
          if (controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
            ret1 = shooter.shoot(Constants.farSpeed, Constants.farSpeedWindow);
          else
            ret1 = shooter.shoot(speed, Constants.midSpeedWindow);

            if (ret1 && ret2 != 1) {
              intake.indexerShoot();
              //Logging.consoleLog("------------------------------------------------------------------");
              //Logging.consoleLog("Robot.java: Firing, speed: " + speed);
              //Logging.consoleLog("Robot.java: Firing, feedback value: " + shooter.getVelocity());
              //Logging.consoleLog("Robot.java: Firing, top photoeye value: " + intake.getTopEye());
            }
            else
              intake.indexToTop();
          //Logging.consoleLog("------------------------------------------------------------------");
          //Logging.consoleLog("Robot.java: LL center passed, LL angle: " + Limelight.getTargetAngleYOffset(Constants.GOAL) );
          //Logging.consoleLog("Robot.java: LL center passed, LL distance: " + Limelight.calculateDistance(Constants.GOAL) );
          //Logging.consoleLog("Robot.java: LL center passed, ret1: " + ret1);
          //Logging.consoleLog("Robot.java: LL center passed, calcSpeed: " + speed);
          //Logging.consoleLog("Robot.java: LL center passed, feedback value: " + shooter.getVelocity());
          //SmartDashboard.putNumber("Calculated shooter distance", Limelight.calculateDistance(Constants.GOAL));
          //SmartDashboard.putNumber("LL Angle Y offset", Limelight.getTargetAngleYOffset(Constants.GOAL));
      }
      // Centering unsuccessful, punt and take a mid shot guess
      else if (ret2 == -1) {
        ret1 = shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
        intake.indexerShoot();
        //Logging.consoleLog("------------------------------------------------------------------");
        //Logging.consoleLog("Robot.java: LL center failed, ret1: " + ret1);
        //Logging.consoleLog("Robot.java: LL center failed, feedback value: " + shooter.getVelocity() );
      }
      // Spin up the shooter while waiting for LL centering to finish
      else if(ret2 == 1) {
        if (controller.getButton(Constants.PILOT, ButtonMap.autoShootFar))
          ret1 = shooter.shoot(Constants.farSpeed, Constants.farSpeedWindow);
        else 
          ret1 = shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
          intake.indexToTop();
        //Logging.consoleLog("------------------------------------------------------------------");
        //Logging.consoleLog("Robot.java: LL prespin, ret1: " + ret1);
        //Logging.consoleLog("Robot.java: LL prespin, feedback value: " + shooter.getVelocity());
        //System.out.println("Spinning up for LL shot");
      }

      SmartDashboard.putNumber("Shooter speed: ", speed);
      //SmartDashboard.putBoolean("ret1 (shoot)", ret1);
      //SmartDashboard.putNumber("ret2 (centerToTarget)", ret2);

        //Logging.consoleLog("------------------------------------------------------------------");
        //Logging.consoleLog("Robot.java: shooter/centering not complete, indexing to top: ");
    }
    else
    // Auto shoot buttons not pressed, so allow driving and look for auto shooting 
    // with non-limelight centering
    {
      runFirstCenter = true;
      drive.resetCenter();
      drive.drive(scaleJoystickAxis(leftPilotX), leftPilotY);

      controller.stopRumble(Constants.PILOT);
      rumbleTimer.stop();
      rumbleTimer.reset();

      // Turn off the limelight
      Limelight.lightOff(Constants.GOAL);

      // give control of shooter/indexer to copilot if not auto shooting
      if (controller.getButton(Constants.COPILOT, ButtonMap.indexerOut))
        intake.indexerOut();
      else if (controller.getButton(Constants.COPILOT, ButtonMap.indexerIn))
        intake.indexerShoot();
      else if (!controller.getButton(Constants.PILOT, ButtonMap.autoShootClose))
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
      // Add the condition here for close shooting without the limelight aim
      // This is a pilot function, for the close shot only
      else if (controller.getButton(Constants.PILOT, ButtonMap.autoShootClose))
      {
        shooter.shooterDown();
        if (shooter.getDown())
        {
         
          boolean ret = shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow);

          if (ret == true)
          {
            shooterAtSpeed = true;
          }
          
          if (shooterAtSpeed)
          {
            //shooter.shooterDown();  // lower the shooter hood
            intake.indexerShoot();
            System.out.println("Taking close hub shot");
          }
          else 
          {
            intake.indexToTop();
          }
        }
        else
        {
          shooter.shoot(Constants.upSpeed, Constants.upSpeedWindow);
        }
      }
      else
      {
        shooter.shootStop();
        shooterAtSpeed = false;
      }
    }

    // copilot commands
    if (controller.getButton(Constants.COPILOT, ButtonMap.shooterUp))
      shooter.shooterUp();
    if (controller.getButton(Constants.COPILOT, ButtonMap.shooterDown))
      shooter.shooterDown();
      
      // Allows for a COPILOT prespin and prevents an issue where COPILOT might accidentally continue holding the prespin button while PILOT is shooting.
      // Temporarily disaled for testing
      //if (controller.getButton(Constants.COPILOT, ButtonMap.shooterPreSpin) && !controller.getButton(Constants.PILOT, ButtonMap.autoShootMid) && !controller.getButton(Constants.PILOT, ButtonMap.autoShootFar) && !controller.getButton(Constants.PILOT, ButtonMap.autoShootClose))
      //shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
      

    dashboardOutput();
  }

  @Override
  public void autonomousInit() 
  {
    state = 0;
    drive.imuZeroYaw();
    drive.initializeEncoders();
    intake.unlockIndex();
    Limelight.lightOn(Constants.GOAL);
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
    //SmartDashboard.putNumber("centerFailTimer", drive.centerFailTimer.get());
		//SmartDashboard.putNumber("CenterIntervalTimer", drive.centerIntervalTimer.get());
    //SmartDashboard.putBoolean("centerInitialized", drive.centerInitialized);
    //SmartDashboard.putNumber("Camera has target:", Limelight.getValidTargets(Constants.GOAL));
    //SmartDashboard.putNumber("Target X (horiz) offset:", Limelight.getTargetAngleXOffset(Constants.GOAL));
    //SmartDashboard.putNumber("Target Y offset:", Limelight.getTargetAngleYOffset(Constants.GOAL));
    //SmartDashboard.putNumber("Left Gyro", drive.getImuYaw(false));
    //SmartDashboard.putNumber("Right Gyro", drive.getImuYaw(true));
    //SmartDashboard.putNumber("Encoder", drive.getRightEncoderPosition());
    //SmartDashboard.putBoolean("Bottom Photoeye:", intake.getBottomEye());
    //SmartDashboard.putBoolean("Top Photoeye:", intake.getTopEye());
    //SmartDashboard.putNumber("Indexer Ball Count", intake.getIndexBallCount());
    //SmartDashboard.putNumber("Target Distance", Limelight.calculateDistance(Constants.GOAL));
    //SmartDashboard.putNumber("LL Angle", Limelight.getTargetAngleYOffset(Constants.GOAL));
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
      ret = drive.turnTo(180.0, 5.0); //first turn
      shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
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
      intake.lockIndex();
      if (shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(2.8))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        intake.unlockIndex();
        state++;
        autoTimer.stop();
        autoTimer.reset();
      }
      break;
    case 4:
      intake.intakeUp();
      intake.stopIntake();
      ret = drive.turnTo(-79.0, 5.0); //second turn
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
      intake.indexToTop();
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
      intake.indexToTop();
      shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
      ret = drive.turnTo(125.0, 5.0); //third turn
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
    intake.indexToTop();
    shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow);
    ret = drive.centerToTarget(0.01, Constants.midLimelightWindow, Constants.GOAL, -1);
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
      intake.lockIndex();
      drive.centerToTarget(0.01, Constants.midLimelightWindow, Constants.GOAL, -1);
      if (shooter.shoot(Constants.midSpeed, Constants.midSpeedWindow))
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(4.0))
      {
        intake.stopIndex();
        autoTimer.stop();
        intake.unlockIndex();
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
      //SmartDashboard.putNumber("autoGyro", drive.getImuYaw(false));
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
      shooter.shoot(Constants.closeSpeed, Constants.twoBallAutoWindow);
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
      intake.lockIndex();
      Boolean autoRet;
      autoRet = shooter.shoot(Constants.closeSpeed, Constants.closeSpeedWindow);
      //SmartDashboard.putBoolean("autoret", autoRet);
      if (autoRet)
        intake.indexerShoot();
      if (autoTimer.advanceIfElapsed(2.5))
      {
        intake.stopIndex();
        autoTimer.stop();
        shooter.shootStop();
        intake.unlockIndex();
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
    intake.unlockIndex();
  }

  // Scale the joystick value to mitigate oversteering
  private double scaleJoystickAxis(double turn)
  {
    double scale = 0.8; // 0.7
    double output;
    //double scaledTurn = scale * turn;

    // Start with a simple linear function for now
    // Might try a more advanced function like a sigmoid
    // later
      //output = turn * scale;
      //output = 1 / (1+Math.pow(Math.exp(1), scale*turn));
      //output = (scaledTurn*1.2) / Math.pow(1+(Math.pow(scaledTurn,2)), 0.5);
      output = (Math.pow(turn, 3) + (0.18*turn)) * scale; 

    // Someone on CD used a cube function, which is cool
    // Result is + for + numbers and - for - numbers
    //output = Math.pow(input, 3);
    return output;
  }

}