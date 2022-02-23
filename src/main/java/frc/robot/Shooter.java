package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.XboxController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.ctre.phoenix.motorcontrol.IFollower;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter {
    private CANSparkMax shooterMotorRight;
    private CANSparkMax shooterMotorLeft;
    private CANSparkMax shooterMotorTurn;
    Controller pilot = new Controller();
    RelativeEncoder encoder;
    private double straightPosition;
    private int currentPosition;
    RelativeEncoder turnEncoder;
    MiniPID pid;
    private DoubleSolenoid solenoidLeft;
    private DoubleSolenoid solenoidRight;
    private boolean shooterDown;

    public Shooter() {
    shooterMotorRight = new CANSparkMax(Constants.shooterMotorRightPort, MotorType.kBrushless);
    shooterMotorLeft = new CANSparkMax(Constants.shooterMotorLeftPort, MotorType.kBrushless);
    shooterMotorTurn = new CANSparkMax(Constants.shooterMotorTurnPort, MotorType.kBrushless);
    //shooterMotorRight.setSmartCurrentLimit(60, 60);
    //shooterMotorLeft.setSmartCurrentLimit(60, 60);
    shooterMotorTurn.setSmartCurrentLimit(60, 60);
    encoder = shooterMotorRight.getEncoder();
    turnEncoder = shooterMotorTurn.getEncoder();
    //straightPosition = 0;
    currentPosition = (int) encoder.getPosition();
    
    solenoidLeft = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 4, 5);
    solenoidRight = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 6, 7);
        
    shooterDown = true;
    shooterDown();
    
    pid = new MiniPID(0.001143, 0.00006, 0.0045);
    }

    public void shootOut(){
        shooterMotorRight.set(0.5);
        shooterMotorLeft.set(0.5);
    }
    public void shootReverse(){
        shooterMotorRight.set(0.5);
        shooterMotorLeft.set(0.5);
    }
    public void shootStop(){
        shooterMotorRight.set(0.0);
        shooterMotorLeft.set(0.0);
    }
    public void shooterTurnRight(){
        shooterMotorTurn.set(0.1); 
    }

    public void shooterTurnLeft(){
        shooterMotorTurn.set(-0.1); 
    }

    public void stopTurn(){
        shooterMotorTurn.set(0.0);
    }


    public void resetTurnEncoder(){
        turnEncoder.setPosition(0.0);
    }
    public void shooterTurnStraight(){
        currentPosition = (int) turnEncoder.getPosition();

        if (currentPosition > 0.5){
            shooterTurnLeft();
            currentPosition = (int) turnEncoder.getPosition();
        } else if (currentPosition < -0.5) {
            shooterTurnRight();
            currentPosition = (int) turnEncoder.getPosition();
        } else {
            stopTurn();
        }
         
    }

    public boolean shoot()
    {
        double setpoint = 1800.0;
        if (setpoint - encoder.getVelocity() < 500)
        {
            pid.setI(0.00006);
            pid.setMaxIOutput(400);
        }
        else
        {
            pid.setI(0.0);
            pid.clearError();
        }
        double speed = pid.getOutput(encoder.getVelocity(), setpoint);
        shooterMotorLeft.set(-speed);
        shooterMotorRight.set(speed);
        SmartDashboard.putNumber("PID Output", speed);
        SmartDashboard.putNumber("ShooterTurnPosition", turnEncoder.getPosition());
        SmartDashboard.putNumber("ShooterEncoder", encoder.getVelocity());
        if (Math.abs(setpoint - encoder.getVelocity()) < 400)
        {
            return true;
        }
        else 
        {
            return false;
        }
        
    }
    public void stickShoot(double stick)
    {
        shooterMotorLeft.set(-0.5 * stick);
        shooterMotorRight.set(0.5 * stick);
    }

    public void shooterUp() {
        solenoidLeft.set(DoubleSolenoid.Value.kForward);
        solenoidRight.set(DoubleSolenoid.Value.kForward);
        shooterDown = false;
    }
    public void shooterDown() {
        solenoidLeft.set(DoubleSolenoid.Value.kReverse);
        solenoidRight.set(DoubleSolenoid.Value.kReverse);
        shooterDown = true;
    }
    public void toggle() {
        if (shooterDown)
            shooterUp();
        else
            shooterDown();
    }
}
