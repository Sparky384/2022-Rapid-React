package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

import java.util.TimerTask;

import com.ctre.phoenix.motorcontrol.IFollower;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.hal.util.CheckedAllocationException;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;

public class Intake {
    
    private DigitalInput bottomPhotoEye;
    public DigitalInput topPhotoEye;
    private CANSparkMax stage1Motor; //4
    private CANSparkMax stage2Motor; //5
    private CANSparkMax stage3Motor; //11
    
    //is true during the setup process for shooting so that the ball is in position. bug when holding shooter joystick while intake is going. 
    private boolean prevTopEye;
    private boolean prevBottomEye;
    private int ballsInIndex;
    
    Timer valveTimer;
    private boolean sValve1;
    private boolean sValve2;
    private boolean timingValve;
    private DoubleSolenoid solenoidLeft;
    private DoubleSolenoid solenoidRight;
    private java.util.Timer intakeTimer; 
    private boolean intakeUp;

    private class TimerCallback extends TimerTask
    {
        // runs at after the intake is put down
        @Override
        public void run() 
        {
            solenoidRight.set(DoubleSolenoid.Value.kOff);
            solenoidLeft.set(DoubleSolenoid.Value.kOff);
        }
    }

    public Intake() {
        stage1Motor = new CANSparkMax(Constants.stage1MotorPort, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        stage2Motor = new CANSparkMax(Constants.stage2MotorPort, MotorType.kBrushless);
        stage2Motor.setSmartCurrentLimit(60, 60);

        stage3Motor = new CANSparkMax(Constants.stage3MotorPort, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        TestSolenoid solenoid = new TestSolenoid();
        //Compressor pcmCompressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
        //pcmCompressor.enableDigital();
        //pcmCompressor.disable();
        sValve1 = solenoid.set(false);
        sValve2 = solenoid.set(true);

        bottomPhotoEye = new DigitalInput(Constants.bottomPhotoEyePort);
        topPhotoEye = new DigitalInput(Constants.topPhotoEyePort);
        prevTopEye = false;
        prevBottomEye = false;
        ballsInIndex = 0;

        solenoidLeft = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
        solenoidRight = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
        
        intakeTimer = new java.util.Timer();
        intakeUp();
    }

    public void intakeUp() {
        solenoidLeft.set(DoubleSolenoid.Value.kReverse);
        solenoidRight.set(DoubleSolenoid.Value.kReverse);
        intakeTimer.cancel();
        intakeUp = true;
    }

    public void intakeDown() {
        solenoidRight.set(DoubleSolenoid.Value.kForward);
        solenoidLeft.set(DoubleSolenoid.Value.kForward);
        intakeTimer.schedule(new TimerCallback(), 500);
        intakeUp = false;
    }

    public void toggle() {
        if (intakeUp)
            intakeDown();
        else
            intakeUp();
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

    public void stopIndex() {
        stage2Motor.set(0);
        stage3Motor.set(0);
    }

    public void indexerShoot(){
        stage2Motor.set(-0.2);
        stage3Motor.set(0.2);
        SmartDashboard.putBoolean("autoIndex", false);
        SmartDashboard.putBoolean("indexerShoot", true);
    }

    public void autoIndex(){
        SmartDashboard.putBoolean("BottomPhotoEyeBlocked", bottomPhotoEye.get());
        SmartDashboard.putBoolean("TopPhotoEyeBlocked", topPhotoEye.get());
        SmartDashboard.putNumber("balls in intake", ballsInIndex);
        SmartDashboard.putBoolean("prev bottom", prevBottomEye);
        SmartDashboard.putBoolean("prev top", prevTopEye);

        boolean top = topPhotoEye.get();
        boolean bottom = bottomPhotoEye.get();
        if (!bottom || top)
        {
            stage2Motor.set(0.0);
            stage3Motor.set(0.0);
        }
        else if (bottom && !top)
        {
            stage2Motor.set(-0.2);
            stage3Motor.set(0.2);
        }
    }  
    
    public void solenoidStartingState(){ 
        sValve1 = false;
        sValve2 = true;
    }

    public int extendedMotion(){       
        
        if (!timingValve){
            valveTimer.reset();
            valveTimer.start();
            timingValve = true;
        }
        sValve1 = true;
        if (valveTimer.hasPeriodPassed(0.5)){
            sValve2 = false;
        }
        if (valveTimer.hasPeriodPassed(1.0)){
            sValve1 = false;
            timingValve = false;
            return 0;
        } else {
            return 1;
        }
    }
    public void collectingState(){
        sValve1 = false;
        sValve2 = false;
    }
    public int retractMotion(){
        if (!timingValve){
            valveTimer.reset();
            valveTimer.start();
            timingValve = true;
        }        
        sValve1 = true;
        sValve2 = true;
        if (valveTimer.hasPeriodPassed(0.5)) {
            sValve1 = false;
            timingValve = false;
            return 0;
        } else {
            return 1;
        }
            
    }

}
