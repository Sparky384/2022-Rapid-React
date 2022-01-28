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
    Controller pilot = new Controller();
    RelativeEncoder encoder;

    public Shooter() {
    shooterMotorRight = new CANSparkMax(13, MotorType.kBrushless);
    shooterMotorLeft = new CANSparkMax(2, MotorType.kBrushless);
    shooterMotorRight.setSmartCurrentLimit(60, 60);
    shooterMotorLeft.setSmartCurrentLimit(60, 60);
    encoder = shooterMotorRight.getEncoder();
    


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
        shooterMotorRight.set(0.5);
        shooterMotorLeft.set(0.5);
    }
    public void shoot(){
        double pilotY = pilot.getRightY();
        //double pilotX = -1 * pilot.getRightX();
        shooterMotorLeft.set(0.175*pilotY);
        shooterMotorRight.set(-0.175*pilotY);
        SmartDashboard.putNumber("ShooterEncoder", encoder.getVelocity());
        
    }
}
