package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

import javax.lang.model.util.ElementScanner6;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter {
    private CANSparkMax shooterMotorRight;
    private CANSparkMax shooterMotorLeft;
    private CANSparkMax shooterMotorTurn;
    private RelativeEncoder encoder;
    private int currentPosition;
    private RelativeEncoder turnEncoder;
    private MiniPID pid;
    private DoubleSolenoid solenoidLeft;
    private DoubleSolenoid solenoidRight;
    private boolean shooterDown;
    public boolean noMore = false;

    public Shooter() {
    shooterMotorRight = new CANSparkMax(Constants.shooterMotorRightPort, MotorType.kBrushless);
    shooterMotorLeft = new CANSparkMax(Constants.shooterMotorLeftPort, MotorType.kBrushless);
    shooterMotorTurn = new CANSparkMax(Constants.shooterMotorTurnPort, MotorType.kBrushless);
    //shooterMotorRight.setSmartCurrentLimit(60, 60);
    //shooterMotorLeft.setSmartCurrentLimit(60, 60);
    shooterMotorTurn.setSmartCurrentLimit(60, 60);
    encoder = shooterMotorRight.getEncoder();
    turnEncoder = shooterMotorTurn.getEncoder();
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
        System.out.println("STAP");
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

    public boolean getDown()
    {
        return shooterDown;
    }

    public boolean testShoot(double speed)
    {
        shooterMotorLeft.set(speed);
        shooterMotorRight.set(-speed);
        SmartDashboard.putNumber("ShooterEncoder", -encoder.getVelocity());
        return true;
    }

    public boolean shoot(double set)
    {
        double P = SmartDashboard.getNumber("Shoot P", Constants.shooterP);
        double I = SmartDashboard.getNumber("Shoot I", Constants.shooterI);
        double D = SmartDashboard.getNumber("Shoot D", Constants.shooterD);
        double Pin = SmartDashboard.getNumber("Shoot Pin", 0.0);

        double setpoint = set;
        double curSpeed = -encoder.getVelocity();
        pid.setI(I);
        pid.setD(D);

        if (setpoint - curSpeed < 1000 && setpoint - curSpeed > 30 ||
            setpoint - curSpeed > -1000 && setpoint - curSpeed < -30)
        {
            pid.setI(I);
            pid.setP(Pin);
            pid.setMaxIOutput(400);
            noMore = true;
        }
        else
        {
                pid.setP(P);
            pid.setI(0.0);
            pid.clearError();
        }
        double speed = pid.getOutput(curSpeed, setpoint);
        shooterMotorLeft.set(speed);
        shooterMotorRight.set(-speed);
        SmartDashboard.putNumber("PID Output", speed);
        SmartDashboard.putNumber("ShooterTurnPosition", turnEncoder.getPosition());
        SmartDashboard.putNumber("ShooterEncoder", curSpeed);
        SmartDashboard.putNumber("Shoot Error", setpoint - curSpeed);
        if (Math.abs(setpoint - curSpeed) < 200)
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
        double speed = SmartDashboard.getNumber("speed", 0.0);
        System.out.printf("%f\n", speed);
        shooterMotorLeft.set(-speed);
        shooterMotorRight.set(speed);
        //shooterMotorLeft.set(-0.5 * stick);
        //shooterMotorRight.set(0.5 * stick);
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
}
