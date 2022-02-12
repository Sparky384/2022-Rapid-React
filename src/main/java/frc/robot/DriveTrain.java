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

    DriveTrain() {
		frontLeft = new WPI_TalonFX(1);
		frontRight = new WPI_TalonFX(14);
		backLeft = new WPI_TalonFX(16);
		backRight = new WPI_TalonFX(15);
		leftMotors = new MotorControllerGroup(frontLeft, backLeft);
		rightMotors = new MotorControllerGroup(frontRight, backRight);
		difDrive = new DifferentialDrive(leftMotors, rightMotors);

        rightMotors.getInverted();
        failTimer = new Timer();
        intervalTimer = new Timer();
        SupplyCurrentLimitConfiguration currentConfig = new SupplyCurrentLimitConfiguration(true, 40, 35, 100);
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
    }

    public void imuZeroYaw() 
	{
        iteration = 0;
        imu.zeroYaw();
    }

    public float getImuYaw() 
	{
        float yaw = imu.getYaw(); //positive values are clockwise
        iteration++;
        yaw += (iteration * 0.0000382);
        return yaw;
    }
    
    public void drive(double speed, double turn) {
        difDrive.arcadeDrive(speed, turn);
    }

    public double getRightEncoderPosition()
	{
		return (frontRight.getSelectedSensorPosition() * ((162.0 * Math.PI) / 634880.0)); 
	}

    public void initializeEncoders()	
	{
		frontLeft.getSensorCollection().setIntegratedSensorPosition(0, 0); 
		frontRight.getSensorCollection().setIntegratedSensorPosition(0, 0);
	}
    
    public int driveTo(double distance, final double timeout) 
    {
		SmartDashboard.putNumber("Current Error", currentError);
		SmartDashboard.putNumber("PID OUT", driveToRate);
		SmartDashboard.putNumber("RightEncoderPosition", getRightEncoderPosition());
        if (!pidInitialized)
        {
            dLock = true;
			max = 0;
			speedController.reset();
			speedController.setPID(Constants.driveP, Constants.driveI, 0); // add PID values here
			speedController.setMaxIOutput(0.3);
			speedController.setOutputLimits(-0.60, 0.60);
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

		if ((currentError < 11.0 && currentError > 0.5) ||
			(currentError > -11.0 && currentError < -0.5))
		{
			if (!inIZone)
			{
				inIZone = true;
				speedController.reset();
				speedController.setI(0.000383);
			}
		}
		else
		{
			inIZone = false;
			speedController.reset();
			speedController.setI(0);
		}

		if (intervalTimer.hasPeriodPassed(1.0))	
		{					// Within deadband for interval time
			failTimer.reset();
			System.out.printf("PID FINISHED %f&&&&&&&&&&&&&&&& (%f in %f)\n", currentError, max, failTimer.get());
			pidInitialized = false;
			return 0;	// PID is complete (successful)
		} 
		else if (failTimer.hasPeriodPassed(timeout)) 	
		{			// the PID has failed!
			System.out.printf("PID FAILED %f&&&&&&&&&&&&&&&&\n", currentError);
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
		SmartDashboard.putNumber("Yaw Error", currentError);
		if (!pidInitialized) 
		{
			speedController.reset();
			speedController.setPID(Constants.turnP, 0, Constants.turnD);
			speedController.setMaxIOutput(0.4);
			speedController.setOutputLimits(-0.65, 0.65);
			speedController.setSetpoint(distance);
			driveToRate = 0;
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
		driveToRate = speedController.getOutput(getImuYaw(), distance);
		currentError = distance - getImuYaw();
		difDrive.arcadeDrive(-driveToRate, 0);
		System.out.printf("t %f\n", currentError);
		SmartDashboard.putNumber("TurnResult", getImuYaw());

		if (Math.abs(currentError) < Constants.turnDeadBand) 	
		{
			if (!timing) 
			{
				intervalTimer.start();
				timing = true;
				System.out.println("intervalStart");
			} 
		} 
		else 	
		{					
			intervalTimer.stop();
			intervalTimer.reset();
			timing = false;
			System.out.println("intervalStop");
		}

		if ((currentError < 11.5 && currentError > 0.2) ||
			(currentError > -11.5 && currentError < -0.2))
		{
			if (!inIZone)
			{
				inIZone = true;
				speedController.reset();
				speedController.setI(Constants.turnI);
			}
		}
		else
		{
			inIZone = false;
			speedController.reset();
			speedController.setI(0);
		}

		if (intervalTimer.hasPeriodPassed(1.0))	
		{					// Within deadband for interval time
			failTimer.reset();
			System.out.printf("PID FINISHED %f\n", currentError);
			pidInitialized = false;
			return 0;	// PID is complete (successful)
		} 
		else if (failTimer.hasPeriodPassed(timeout)) 	
		{			// the PID has failed!
			System.out.printf("PID FAILED %f\n", currentError);
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
}
