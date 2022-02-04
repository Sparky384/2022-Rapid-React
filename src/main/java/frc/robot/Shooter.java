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
    
    SmartDashboard.putNumber("P", 1.0);
    SmartDashboard.putNumber("I", 0.0);
    SmartDashboard.putNumber("D", 0.0);
    pid = new MiniPID(1.0, 0.0, 0.0);
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

    public void shoot(){
        //double pilotY = pilot.getRightY();
        //double pilotX = -1 * pilot.getRightX();
        pid.setP(SmartDashboard.getNumber("P", 0.0));
        if (700.0 - encoder.getVelocity() > 350)
        {
            pid.setMaxIOutput(400);
            pid.setI(SmartDashboard.getNumber("I", 0.0));
        }
        else
        {
            pid.setI(0.0);
            pid.clearError();
        }
        pid.setI(SmartDashboard.getNumber("I", 0.0));
        pid.setD(SmartDashboard.getNumber("D", 0.0));
        double speed = pid.getOutput(encoder.getVelocity(), 700.0);
        //shooterMotorLeft.set(0.175*pilotY);
        //shooterMotorRight.set(-0.175*pilotY);
        shooterMotorLeft.set(speed);
        shooterMotorRight.set(-speed);
        SmartDashboard.putNumber("PID Output", speed);
        SmartDashboard.putNumber("ShooterTurnPosition", turnEncoder.getPosition());
        SmartDashboard.putNumber("ShooterEncoder", encoder.getVelocity());
        /*if (encoder.getVelocity() > 350)
        {
            return true;
        }
        else 
        {
            return false;
        }*/
    }
}
