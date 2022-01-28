package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.ctre.phoenix.motorcontrol.IFollower;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.wpilibj.DigitalInput;

public class Intake {
    
    private DigitalInput bottomPhotoEye;
    private DigitalInput topPhotoEye;
    private CANSparkMax stage1Motor; //4
    private CANSparkMax stage2Motor; //5
    private CANSparkMax stage3Motor; //11
    private boolean ballPhotoEye;
    //private Controller pilot = new Controller();

    public Intake() {
        stage1Motor = new CANSparkMax(4, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        stage2Motor = new CANSparkMax(5, MotorType.kBrushless);
        stage2Motor.setSmartCurrentLimit(60, 60);

        stage3Motor = new CANSparkMax(11, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        bottomPhotoEye = new DigitalInput(0);
        topPhotoEye = new DigitalInput(2);
        ballPhotoEye = false;
        

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
    }

    public void stopIndexer(){
        stage2Motor.set(0);
        stage3Motor.set(0);
    }

    public void autoIndex(){
        SmartDashboard.putBoolean("BottomPhotoEyeBlocked", bottomPhotoEye.get());
        SmartDashboard.putBoolean("TopPhotoEyeBlocked", topPhotoEye.get());
        // if (bottomPhotoEye.get() && !topPhotoEye.get()){ // dont use loop, loops are bad
        //     stage2Motor.set(-0.2);
        //     stage3Motor.set(0.2);
        // } else {
        // stage2Motor.set(0.0);
        // stage3Motor.set(0.0);
        // }
        if ((ballPhotoEye || bottomPhotoEye.get()) && !topPhotoEye.get())
        {
            stage2Motor.set(-0.2);
            stage3Motor.set(0.2);
            ballPhotoEye = true;
        }
        else {
            stage2Motor.set(0.0);
            stage3Motor.set(0.0);
            ballPhotoEye = false;
        }
    }    
}
