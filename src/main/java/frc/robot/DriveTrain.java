package frc.robot;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import javax.lang.model.util.ElementScanner6;

import com.ctre.phoenix.motorcontrol.ControlMode;


public class DriveTrain {
    WPI_TalonFX frontLeft;
    WPI_TalonFX frontRight;
    WPI_TalonFX backLeft;
    WPI_TalonFX backRight;
    MotorControllerGroup leftMotors;
    MotorControllerGroup rightMotors;
    DifferentialDrive difDrive;
    AHRS imu;
    int iteration;
    MiniPID speedController;
    boolean pidInitialized;
    boolean timing;
    boolean inIZone;
    boolean dLock;
    Timer failTimer;
    Timer intervalTimer;
    double driveToRate;
    double currentError;
    double max;
	Timer centerFailTimer;
	Timer centerIntervalTimer;
	boolean centerTiming;
	boolean centerInitialized;
	double tempP;
	double tempI;
	double tempD;
	boolean prevFinished;

    DriveTrain() {
		tempP = 0.0;
		tempI = 0.0;
		tempD = 0.0;

		pidInitialized = false;
		prevFinished = false;

		frontLeft = new WPI_TalonFX(Constants.frontLeftPort);
		frontRight = new WPI_TalonFX(Constants.frontRightPort);
		backLeft = new WPI_TalonFX(Constants.backLeftPort);
		backRight = new WPI_TalonFX(Constants.backRightPort);
		leftMotors = new MotorControllerGroup(frontLeft, backLeft);
		rightMotors = new MotorControllerGroup(frontRight, backRight);
		// Comment this out when using new drive code
		difDrive = new DifferentialDrive(leftMotors, rightMotors);
		// *******************************************************
		centerIntervalTimer = new Timer();
		centerFailTimer = new Timer();

        rightMotors.getInverted();
        failTimer = new Timer();
        intervalTimer = new Timer();
        SupplyCurrentLimitConfiguration currentConfig = new SupplyCurrentLimitConfiguration(true, 80, 100, 5);
        iteration = 0;

        frontRight.configSupplyCurrentLimit(currentConfig);
        backRight.configSupplyCurrentLimit(currentConfig);
        frontLeft.configSupplyCurrentLimit(currentConfig);
        backLeft.configSupplyCurrentLimit(currentConfig);

        frontRight.setSafetyEnabled(false);
        backRight.setSafetyEnabled(false);
        frontLeft.setSafetyEnabled(false);
        backLeft.setSafetyEnabled(false);

        frontRight.setNeutralMode(NeutralMode.Coast);
        backRight.setNeutralMode(NeutralMode.Coast);
        frontLeft.setNeutralMode(NeutralMode.Coast);
        backLeft.setNeutralMode(NeutralMode.Coast);

        frontRight.setInverted(false);
        backRight.setInverted(false);
        frontLeft.setInverted(false);
        backLeft.setInverted(false);

        frontLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);    
        frontLeft.setSensorPhase(true);  

        backLeft.follow(frontLeft);
        backRight.follow(frontRight);

        frontRight.configOpenloopRamp(0.75,0);
        backRight.configOpenloopRamp(0.75,0);
        frontLeft.configOpenloopRamp(0.75,0);
        backLeft.configOpenloopRamp(0.75,0);

        imu = new AHRS(SPI.Port.kMXP);
        speedController = new MiniPID(0, 0, 0);
		max = 0;

		//SmartDashboard.putNumber("a", 0);
		//SmartDashboard.putNumber("b", 1);

    }

	public void stop()
	{
		difDrive.arcadeDrive(0, 0);
	}

	public void resetPid()
	{
		pidInitialized = false;
	}

    public void imuZeroYaw() 
	{
        iteration = 0;
        imu.zeroYaw();
    }

    public float getImuYaw(boolean isRight) 
	{
        float yaw = imu.getYaw(); //positive values are clockwise
        if (isRight)
		{
			if (yaw < -45)
			{
				yaw = 180 + (180 - Math.abs(yaw));
			}
		}
		else
		{
			if (yaw > 45)
			{
				yaw = -180 - (180 - yaw);
			}
		}
		iteration++;
        yaw += (iteration * 0.0000382);
        return yaw;
    }

    public void drive(double speed, double turn) 
	{
		difDrive.arcadeDrive(speed, turn);
	}

    public double getRightEncoderPosition()
	{
		return (frontRight.getSelectedSensorPosition() * ((150.58 * Math.PI) / 634880.0)); 
	}

    public void initializeEncoders()	
	{
		frontLeft.getSensorCollection().setIntegratedSensorPosition(0, 0); 
		frontRight.getSensorCollection().setIntegratedSensorPosition(0, 0);
	}
    
    public int driveTo(double distance, final double timeout, boolean isShortDist) 
    {
		//SmartDashboard.putNumber("Current Error", currentError);
		//SmartDashboard.putNumber("PID OUT", driveToRate);
		//SmartDashboard.putNumber("RightEncoderPosition", getRightEncoderPosition());
		double P;
		double I;
		double D;
		if (isShortDist)
		{
			P = Constants.driveShortP;
			I = Constants.driveShortI;
			D = Constants.driveShortD;
		}
		else
		{
			P = Constants.driveLongP;
			I = Constants.driveLongI;
			D = Constants.driveLongD;
		}
		double IL = Constants.driveIZoneLower;
		double IU = Constants.driveIZoneUpper;
        if (!pidInitialized)
        {
            dLock = true;
			max = 0;
			speedController.reset();
			speedController.setPID(P, I, D); // add PID values here
			speedController.setMaxIOutput(0.3);
			speedController.setOutputLimits(-0.85, 0.85);
			speedController.setSetpoint(distance);
			driveToRate = 0;
			failTimer.reset();
			failTimer.start();			// the PID will fail if this timer exceeded
			pidInitialized = true;
			currentError = 0;
			timing = false;
			intervalTimer.stop();
			intervalTimer.reset();
			initializeEncoders();
			while (getRightEncoderPosition() < -2 || getRightEncoderPosition() > 2)
			{
				System.out.println(getRightEncoderPosition()); // wait
			}
        }
        // This is the final output of the PID

		driveToRate = speedController.getOutput(getRightEncoderPosition(), distance);
		currentError = distance - (getRightEncoderPosition());
		//SmartDashboard.putNumber("drive error", currentError);
		difDrive.arcadeDrive(0, -driveToRate);
		if (getRightEncoderPosition() > max)
			max = getRightEncoderPosition();

		// robot drove backward on rare occasions
		// beleive this is from the derivative being very negative in the beginning
		// possibly caused by undefined derivitave at the first point
		// make sure we moved some before calculating D
		if (dLock && Math.abs(getRightEncoderPosition()) > 15)
		{
			dLock = false;
			speedController.setD(1.55); //set d value
		}
		
		if (Math.abs(currentError) < Constants.deadBand) 	
		{
			if (!timing) 
			{
				intervalTimer.start();
				timing = true;
			} 
		} 
		else 	
		{					
			intervalTimer.stop();
			intervalTimer.reset();
			timing = false;
		}

		if ((currentError < IU && currentError > IL) ||
			(currentError > -IU && currentError < -IL))
		{
			if (!inIZone)
			{
				inIZone = true;
				speedController.reset();
				speedController.setI(I);
			}
		}
		else
		{
			inIZone = false;
			speedController.reset();
			speedController.setI(0);
		}

		if (intervalTimer.hasPeriodPassed(0.8))	
		{					// Within deadband for interval time
			failTimer.reset();
			pidInitialized = false;
			return 0;	// PID is complete (successful)
		} 
		else if (failTimer.hasPeriodPassed(timeout)) 	
		{			// the PID has failed!
			frontLeft.set(ControlMode.PercentOutput, 0);		// stop the motors
			frontRight.set(ControlMode.PercentOutput, 0);
			intervalTimer.stop();
			failTimer.stop();
			intervalTimer.reset();
			failTimer.reset();
			speedController.reset();
			pidInitialized = false;
			return -1;
		} 
		else	
		{	// the PID is not complete
			return 1;
		}
    }

	public int turnTo(double distance, final double timeout)	
	{	
		double P = Constants.turnP;
		double I = Constants.turnI;
		double D = Constants.turnD;
		double IL = Constants.turnIZoneLower;
		double IU = Constants.turnIZoneUpper;
		boolean isUTurn = false;

		if (!pidInitialized) 
		{
			speedController.reset();
			speedController.setPID(P, I, D); //i was orignally 0
			speedController.setMaxIOutput(0.4);
			speedController.setOutputLimits(-0.65, 0.65);
			speedController.setSetpoint(distance);
			driveToRate = 0;
			failTimer.reset();
			failTimer.start();			// the PID will fail if this timer exceeded
			pidInitialized = true;
			currentError = 0;
			timing = false;
			intervalTimer.stop();
			intervalTimer.reset();
			imuZeroYaw();
			inIZone = false;
		}
		// This is the final output of the PID
		boolean isRight;
		if (distance > 0)
			isRight = true;
		else
			isRight = false;

		double curAngle = getImuYaw(isRight);
		driveToRate = speedController.getOutput(curAngle, distance);
		currentError = distance - curAngle;
		//if (distance < 0)
		//	driveToRate *= -1;
		difDrive.arcadeDrive(-driveToRate, 0);
		//SmartDashboard.putNumber("TurnResult", getImuYaw(isUTurn));
		//SmartDashboard.putNumber("Yaw Error", currentError);
		//SmartDashboard.putNumber("Not TurnResult", getImuYaw(!isUTurn));
		//SmartDashboard.putBoolean("uturn", isUTurn);

		if (Math.abs(currentError) < Constants.turnDeadBand) 	
		{
			if (!timing) 
			{
				intervalTimer.start();
				timing = true;
			} 
		} 
		else 	
		{					
			intervalTimer.stop();
			intervalTimer.reset();
			timing = false;
		}

		if ((currentError < IU && currentError > IL) ||
			(currentError > -IU && currentError < -IL))
		{
			if (!inIZone)
			{
				inIZone = true;
				speedController.reset();
				speedController.setI(I);
			}
		}
		else
		{
			inIZone = false;
			speedController.reset();
			speedController.setI(0);
		}

		if (intervalTimer.hasPeriodPassed(0.05)) // 0.5	
		{					// Within deadband for interval time
			failTimer.reset();
			pidInitialized = false;
			return 0;	// PID is complete (successful)
		} 
		else if (failTimer.hasPeriodPassed(timeout)) 	
		{			// the PID has failed!
			frontLeft.set(ControlMode.PercentOutput, 0);		// stop the motors
			frontRight.set(ControlMode.PercentOutput, 0);
			intervalTimer.stop();
			failTimer.stop();
			intervalTimer.reset();
			failTimer.reset();
			speedController.reset();
			pidInitialized = false;
			return -1;
		} 
		else	
		{	// the PID is not complete
			return 1;
		}
	}
	
	public void resetCenter()
	{
		centerInitialized = false;
		prevFinished = false;
	}

	public int centerToTarget(double timeout, double window, String cam)
	{
		double error = Limelight.getTargetAngleXOffset(Constants.GOAL);
		//SmartDashboard.putNumber("erer", error);
		//SmartDashboard.putNumber("window", window);
		//SmartDashboard.putNumber("timeout", timeout);
		if(!centerInitialized)
		{
			prevFinished = false;
			centerTiming = false;
			centerIntervalTimer.stop();
			centerIntervalTimer.reset();
			centerFailTimer.reset();
			centerFailTimer.start();
			centerInitialized = true;
			//System.out.println("init--------------------------------------------------------");
		}
		if(error < window && error > -window)
		{
			difDrive.arcadeDrive(0.0, 0.0);
			centerIntervalTimer.start();
			centerTiming = true;
		}
		else if(error < window)
			difDrive.arcadeDrive(0.45, 0.0); //0.325
		else if(error > -window)
			difDrive.arcadeDrive(-0.45, 0.0); //-0.325

		if(prevFinished || centerIntervalTimer.hasPeriodPassed(Constants.centerIntervalTime))
		{
			centerFailTimer.stop();
			//centerFailTimer.reset();
			//centerIntervalTimer.stop();
			difDrive.arcadeDrive(0.0, 0.0);
			//centerInitialized = false;
			prevFinished = true;
			return 0;
		}
		else if (centerFailTimer.hasPeriodPassed(timeout))
		{
			centerIntervalTimer.stop();
			//centerIntervalTimer.reset();
			//centerFailTimer.stop();
			difDrive.arcadeDrive(0.0, 0.0);
			//centerInitialized = false;
			return -1;
		}
		return 1;
	}
}
