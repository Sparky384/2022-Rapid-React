package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

import java.util.Timer;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.revrobotics.RelativeEncoder;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import com.ctre.phoenix.motorcontrol.NeutralMode;

public class DriveTrain {
    WPI_TalonFX frontLeft = new WPI_TalonFX(1);
    WPI_TalonFX frontRight = new WPI_TalonFX(14);
    WPI_TalonFX backLeft = new WPI_TalonFX(16);
    WPI_TalonFX backRight = new WPI_TalonFX(15);
    //WPI_TalonFX frontLeft;
    //WPI_TalonFX frontRight;
    //WPI_TalonFX backLeft;
    //WPI_TalonFX backRight;
    MotorControllerGroup leftMotors = new MotorControllerGroup(frontLeft, backLeft);
    MotorControllerGroup rightMotors = new MotorControllerGroup(frontRight, backRight);
    Controller pilot = new Controller();
    DifferentialDrive difDrive = new DifferentialDrive(leftMotors, rightMotors);
    //motorvariable.congifSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10)
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


    DriveTrain() {
        //frontLeft = new WPI_TalonFX(1); - Had to define the Talons above with the motor type.
        //frontRight = new WPI_TalonFX(14); - Had to define the Talons above with the motor type.
        //backLeft = new WPI_TalonFX(16); - Had to define the Talons above with the motor type.
        //backRight = new WPI_TalonFX(15); - Had to define the Talons above with the motor type.
        //rightMotors.getInverted();
        failTimer = new Timer();
        intervalTimer = new Timer();
        SupplyCurrentLimitConfiguration currentConfig = new SupplyCurrentLimitConfiguration
        (true, 40, 35, 100);
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

        frontLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative,
                                     0,
                                     10);    
        frontLeft.setSensorPhase(true);  

        backLeft.follow(frontLeft);
        backRight.follow(frontRight);

        frontRight.configOpenloopRamp(0.75,0);
        backRight.configOpenloopRamp(0.75,0);
        frontLeft.configOpenloopRamp(0.75,0);
        backLeft.configOpenloopRamp(0.75,0);


        imu = new AHRS(SPI.Port.kMXP);
        speedController = new MiniPID(0, 0, 0);
    }

    public void imuZeroYaw() {
        iteration = 0;
        imu.zeroYaw();
    }
    public float getImuYaw() {
        float yaw = imu.getYaw();
        iteration++;
        yaw += (iteration * 0.0000382);
        return yaw;
    }
    

    public void drive() {
        double pilotY = 0.8*pilot.getLeftY();
        double pilotX = -0.8 * pilot.getLeftX();
        difDrive.arcadeDrive(pilotX, pilotY);
        
    }
}
