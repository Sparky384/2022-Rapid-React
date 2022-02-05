package frc.robot;

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

    public Shooter() {
    shooterMotorRight = new CANSparkMax(13, MotorType.kBrushless);
    shooterMotorLeft = new CANSparkMax(2, MotorType.kBrushless);
    shooterMotorTurn = new CANSparkMax(10, MotorType.kBrushless);
    shooterMotorRight.setSmartCurrentLimit(60, 60);
    shooterMotorLeft.setSmartCurrentLimit(60, 60);
    shooterMotorTurn.setSmartCurrentLimit(60, 60);
    encoder = shooterMotorLeft.getEncoder();
    turnEncoder = shooterMotorTurn.getEncoder();
    //straightPosition = 0;
    currentPosition = (int) encoder.getPosition();
    
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

    public boolean shoot(){
        //double pilotY = pilot.getRightY();
        //double pilotX = -1 * pilot.getRightX();
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
        double speed = pid.getOutput(-encoder.getVelocity(), setpoint);
        //shooterMotorLeft.set(0.175*pilotY);
        //shooterMotorRight.set(-0.175*pilotY);
        shooterMotorLeft.set(-speed);
        shooterMotorRight.set(speed);
        SmartDashboard.putNumber("PID Output", speed);
        SmartDashboard.putNumber("ShooterTurnPosition", turnEncoder.getPosition());
        SmartDashboard.putNumber("ShooterEncoder", encoder.getVelocity());
        if (Math.abs(setpoint - (-encoder.getVelocity())) < 400)
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
}
