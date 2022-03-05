package frc.robot;


import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Timer;

public class Intake {
    
    private DigitalInput bottomPhotoEye;
    public DigitalInput topPhotoEye;
    private CANSparkMax stage1Motor; //4
    private CANSparkMax stage3Motor; //11
    
    //is true during the setup process for shooting so that the ball is in position. bug when holding shooter joystick while intake is going. 
    private boolean prevTopEye;
    private boolean prevBottomEye;
    private int ballsInIndex;
    
    private DoubleSolenoid rearSolenoid;
    private DoubleSolenoid frontSolenoid;
    private Timer intakeTimer; 
    private final int UP = 0;
    private final int DOWN = 1;
    private final int COLLECT = 2;
    private int intakeState;
    private double waitTime = 1.0;

    private final DoubleSolenoid.Value on = DoubleSolenoid.Value.kReverse;
    private final DoubleSolenoid.Value off = DoubleSolenoid.Value.kForward;
    private final DoubleSolenoid.Value open = DoubleSolenoid.Value.kOff;

    public Intake() {
        stage1Motor = new CANSparkMax(Constants.stage1MotorPort, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        stage3Motor = new CANSparkMax(Constants.stage3MotorPort, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        bottomPhotoEye = new DigitalInput(Constants.bottomPhotoEyePort);
        topPhotoEye = new DigitalInput(Constants.topPhotoEyePort);
        prevTopEye = false;
        prevBottomEye = false;
        ballsInIndex = 0;

        rearSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 5, 4);
        frontSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 3, 2);
        
        intakeTimer = new Timer();
        intakeState = DOWN;
        intakeUp();
    }

    public void intakeUp() {
        if (intakeState == DOWN || intakeState == COLLECT)
        {
            rearSolenoid.set(off);
            frontSolenoid.set(on);
            intakeTimer.stop();
            intakeState = UP;
        }
    }

    public void intakeDown() {
            if (intakeState != COLLECT)
            {
                rearSolenoid.set(on);
                frontSolenoid.set(off);
            }
            if (intakeState == UP)
            {
                intakeTimer.reset();
                intakeTimer.start();
            }
            else if (intakeTimer.hasElapsed(waitTime))
            {
                rearSolenoid.set(off);
                frontSolenoid.set(off);
                intakeState = COLLECT;
            }
            intakeState = DOWN;
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
        stage3Motor.set(0);
    }

    public void indexerShoot(){
        stage3Motor.set(0.5);
    }

    public void indexerOut(){
        stage3Motor.set(-0.5);
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
