package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

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
    //private DoubleSolenoid solenoidRight;
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
        
        solenoidLeft = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
        //solenoidRight = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 6, 7);
            
        shooterDown = true;
        shooterDown();
        
        pid = new MiniPID(0.001143, 0.00006, 0.0045);
    }

    public void shootOut(){
        shooterMotorRight.set(0.5);
        shooterMotorLeft.set(0.5);
    }

    public void shootStop(){
        shooterMotorRight.set(0.0);
        shooterMotorLeft.set(0.0);
    }

    public boolean getDown()
    {
        return shooterDown;
    }

    public boolean testShoot(double speed)
    {
        if (speed > 1.0 || speed < -1.0)
            speed /= 100;
        shooterMotorLeft.set(speed);
        shooterMotorRight.set(-speed);
        SmartDashboard.putNumber("ShooterEncoder", -encoder.getVelocity());
        return true;
    }

    public boolean shoot(double set)
    {
        double F = ((0.0182 * set) - 0.022) / 100.0; // formula found experimentally
        double Pin = Constants.shooterP;
        double Iin = Constants.shooterI;
        double Din = Constants.shooterD;

        double setpoint = set;
        double curSpeed = -encoder.getVelocity();
        double error = setpoint - curSpeed;
        if (error < 300 && error > 3 ||
            error > -300 && error < -3)
        {
            pid.setI(Iin);
            pid.setD(Din);
            pid.setP(Pin);
            pid.setMaxIOutput(400);
            noMore = true;
        }
        else
        {
            if (error > 300 || error < -300)
            {
                pid.setD(0.0);
                pid.setP(0.0);
                pid.setI(0.0);
            }
            pid.clearError();
        }
        double speed = pid.getOutput(curSpeed, setpoint) + F;
        shooterMotorLeft.set(speed);
        shooterMotorRight.set(-speed);
        SmartDashboard.putNumber("PID Output", speed);
        SmartDashboard.putNumber("ShooterTurnPosition", turnEncoder.getPosition());
        SmartDashboard.putNumber("ShooterEncoder", curSpeed);
        SmartDashboard.putNumber("Shoot Error", setpoint - curSpeed);
        if (Math.abs(error) < 150)
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
        shooterMotorLeft.set(-speed);
        shooterMotorRight.set(speed);
    }

    public void shooterUp() {
        solenoidLeft.set(DoubleSolenoid.Value.kForward);
        shooterDown = false;
    }

    public void shooterDown() {
        solenoidLeft.set(DoubleSolenoid.Value.kReverse);
        shooterDown = true;
    }
}
