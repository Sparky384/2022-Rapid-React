package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.ctre.phoenix.motorcontrol.IFollower;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.hal.util.CheckedAllocationException;
import edu.wpi.first.wpilibj.DigitalInput;

public class Intake {
    
    private DigitalInput bottomPhotoEye;
    public DigitalInput topPhotoEye;
    private CANSparkMax stage1Motor; //4
    private CANSparkMax stage2Motor; //5
    private CANSparkMax stage3Motor; //11
    //is true during the setup process for shooting so that the ball is in position. bug when holding shooter joystick while intake is going. 
    private boolean ballInBetween; 
    //private Controller pilot = new Controller();
    private boolean prevTopEye;
    private boolean prevBottomEye;
    private int ballsInIndex;

    public Intake() {
        stage1Motor = new CANSparkMax(4, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        stage2Motor = new CANSparkMax(5, MotorType.kBrushless);
        stage2Motor.setSmartCurrentLimit(60, 60);

        stage3Motor = new CANSparkMax(11, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        bottomPhotoEye = new DigitalInput(0);
        topPhotoEye = new DigitalInput(2);
        ballInBetween = false;
        prevTopEye = false;
        prevBottomEye = false;
        ballsInIndex = 0;
    }

    public void intakeIn() {
        stage1Motor.set(0.5);
    }

    public void intakeOut() {
        stage1Motor.set(-0.5);
    }

    public void stopIntake() {
        stage1Motor.set(0);
    }

    public void indexerShoot(){
        stage2Motor.set(-0.2);
        stage3Motor.set(0.2);
        SmartDashboard.putBoolean("autoIndex", false);
        SmartDashboard.putBoolean("indexerShoot", true);
        checkEyes();
    }

    public void stopIndexer(){
        stage2Motor.set(0);
        stage3Motor.set(0);
        ballInBetween = false;
    }

    public void checkEyes()
    {
        SmartDashboard.putBoolean("BottomPhotoEyeBlocked", bottomPhotoEye.get());
        SmartDashboard.putBoolean("TopPhotoEyeBlocked", topPhotoEye.get());
        SmartDashboard.putBoolean("ballBetween", ballInBetween);
        if (ballInBetween)
                System.out.printf("ball change 3 %b %b\n", topPhotoEye.get(), prevTopEye);
            
        if (topPhotoEye.get() == true && prevTopEye == false && ballInBetween == true);
        {
            if (ballInBetween)
                System.out.printf("ball change 2 %b %b\n", topPhotoEye.get(), prevTopEye);
            ballInBetween = false;
        }

        if (bottomPhotoEye.get() || ballInBetween)
            ballInBetween = true;

        prevTopEye = topPhotoEye.get();
    }

    public void autoIndex(){
        SmartDashboard.putBoolean("BottomPhotoEyeBlocked", bottomPhotoEye.get());
        SmartDashboard.putBoolean("TopPhotoEyeBlocked", topPhotoEye.get());
        SmartDashboard.putBoolean("ballBetween", ballInBetween);

        if (topPhotoEye.get() || (!topPhotoEye.get() && !bottomPhotoEye.get() && !ballInBetween))
        {
            stage2Motor.set(0.0);
            stage3Motor.set(0.0);
            if (ballInBetween)
                System.out.println("ball change 1");
            ballInBetween = false;
        }
        else if (bottomPhotoEye.get() || (ballInBetween && !topPhotoEye.get() && !bottomPhotoEye.get()))
        {
            ballInBetween = true;
            stage2Motor.set(-0.2);
            stage3Motor.set(0.2);
        }
        prevTopEye = topPhotoEye.get();
        prevBottomEye = bottomPhotoEye.get();
    }    
}
