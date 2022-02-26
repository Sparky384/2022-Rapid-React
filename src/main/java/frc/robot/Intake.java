package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;

public class Intake {
    
    private DigitalInput bottomPhotoEye;
    public DigitalInput topPhotoEye;
    private CANSparkMax stage1Motor; //4
    //private CANSparkMax stage2Motor; //5
    private CANSparkMax stage3Motor; //11
    
    //is true during the setup process for shooting so that the ball is in position. bug when holding shooter joystick while intake is going. 
    private boolean prevTopEye;
    private boolean prevBottomEye;
    private int ballsInIndex;
    
    /*private DoubleSolenoid rearSolenoid;
    private DoubleSolenoid frontSolenoid;
    private java.util.Timer intakeTimer; 
    private final int UP = 0;
    private final int DOWN = 1;
    private final int COLLECT = 2;
    private int intakeState;
    private int waitTime = 3000; // in ms

    private final DoubleSolenoid.Value on = DoubleSolenoid.Value.kForward;
    private final DoubleSolenoid.Value off = DoubleSolenoid.Value.kReverse;
    private final DoubleSolenoid.Value open = DoubleSolenoid.Value.kOff;

    private class TimerCallbackEnd extends TimerTask
    {
        // runs at after the intake is put down
        @Override
        public void run() 
        {
            rearSolenoid.set(off);
            intakeState = COLLECT;
        }
    }

    private class TimerCallbackMid extends TimerTask
    {
        // runs at after the intake is put down
        @Override
        public void run() 
        {
            frontSolenoid.set(off);
            intakeTimer.cancel();
            intakeTimer.schedule(new TimerCallbackEnd(), waitTime);
        }
    }*/

    public Intake() {
        stage1Motor = new CANSparkMax(Constants.stage1MotorPort, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        /*stage2Motor = new CANSparkMax(Constants.stage2MotorPort, MotorType.kBrushless);
        stage2Motor.setSmartCurrentLimit(60, 60);
        */
        stage3Motor = new CANSparkMax(Constants.stage3MotorPort, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        bottomPhotoEye = new DigitalInput(Constants.bottomPhotoEyePort);
        topPhotoEye = new DigitalInput(Constants.topPhotoEyePort);
        prevTopEye = false;
        prevBottomEye = false;
        ballsInIndex = 0;

        //rearSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
        //frontSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 2, 3);
        
        //intakeTimer = new java.util.Timer();
        //intakeState = UP;
        //intakeUp();
    }

    /*public void intakeUp() {
        if (intakeState == DOWN || intakeState == COLLECT)
        {
            rearSolenoid.set(off);
            frontSolenoid.set(on);
            intakeTimer.cancel();
            intakeState = UP;
        }
    }

    public void intakeDown() {
        if (intakeState == UP)
        {
            rearSolenoid.set(on);
            frontSolenoid.set(on);
            intakeTimer.schedule(new TimerCallbackMid(), waitTime);
            intakeState = DOWN;
        }
    }*/

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
        //stage2Motor.set(0);
        stage3Motor.set(0);
    }

    public void indexerShoot(){
        //stage2Motor.set(-0.2);
        stage3Motor.set(0.5);
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
            //stage2Motor.set(0.0);
            stage3Motor.set(0.0);
        }
        else if (bottom && !top)
        {
            //stage2Motor.set(-0.2);
            stage3Motor.set(0.5);
        }
    }
}
