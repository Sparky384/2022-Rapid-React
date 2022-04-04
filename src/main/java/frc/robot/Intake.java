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
    
    private DoubleSolenoid rearSolenoid;
    private DoubleSolenoid frontSolenoid;
    private Timer intakeTimer; 
    private final int UP = 0;
    private final int DOWN = 1;
    private final int COLLECT = 2;
    private int intakeState;
    private double waitTime = 1.0;
    private boolean indexLock;

    
    private final DoubleSolenoid.Value on = DoubleSolenoid.Value.kReverse;
    private final DoubleSolenoid.Value off = DoubleSolenoid.Value.kForward;
    private final DoubleSolenoid.Value open = DoubleSolenoid.Value.kOff;

    // Track number of balls in indexer
    //private Timer ballIndexEntryTimer;
    //private Timer ballIndexExitTimer;
    //private int ballsInIndex;
    //private boolean ballEntered;
    //private boolean ballExited;


    public Intake() {
        stage1Motor = new CANSparkMax(Constants.stage1MotorPort, MotorType.kBrushless);
        stage1Motor.setSmartCurrentLimit(60, 60);

        stage3Motor = new CANSparkMax(Constants.stage3MotorPort, MotorType.kBrushless);
        stage3Motor.setSmartCurrentLimit(60, 60);

        bottomPhotoEye = new DigitalInput(Constants.bottomPhotoEyePort);
        topPhotoEye = new DigitalInput(Constants.topPhotoEyePort);
        prevTopEye = false;
        prevBottomEye = false;
        //ballsInIndex = 0;

        rearSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 5, 4);
        frontSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 3, 2);
        
        intakeTimer = new Timer();
        intakeState = DOWN;
        intakeUp();

        indexLock = false;

        //ballIndexEntryTimer = new Timer();
        //ballIndexExitTimer = new Timer();
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
        stage1Motor.set(Constants.intakeSpeed);
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
        stage3Motor.set(Constants.indexerSpeed);
    }

    public void indexerOut(){
        stage3Motor.set(-0.5);
    }

    public boolean getBottomEye()
    {
        return bottomPhotoEye.get();
    }

    public boolean getTopEye()
    {
        return topPhotoEye.get();
    }

    public void autoIndex(){
        boolean top = topPhotoEye.get();
        boolean bottom = bottomPhotoEye.get();
       if (!indexLock) {
            if (!bottom || top)
            {
                stage3Motor.set(0.0);
            }
            else if (bottom && !top)
            {
                stage3Motor.set(0.75);
            }
        }
    }

    public void lockIndex(){
        if (!indexLock)
            stage3Motor.set(0.0);
        indexLock = true;
    }

    public void unlockIndex(){
        indexLock = false;
    }
    public void indexToTop(){
        boolean top = topPhotoEye.get();
        if(!top){
            stage3Motor.set(Constants.indexerSpeed);
        }
        else {
            stage3Motor.set(0.0);
        }
    }

    /*

    // Updates the number of balls in the indexer
    // Used to optimize timing for the autoshooting
    // plus the 3-ball autonomous mode
    public void updateIndexBallCount()
    {
        boolean bottom = bottomPhotoEye.get();
        boolean top = topPhotoEye.get();

        // A ball is just detected in the intake
        if (bottom)
        {
            ballIndexEntryTimer.start();
        }

        // A ball has been in the intake PE for a sufficient
        // amount of time, and then passed it
        if (ballIndexEntryTimer.hasElapsed(1.0) && !bottom)
        {
            ballEntered = true;
            ballIndexEntryTimer.reset();
            setIndexBallCount(getIndexBallCount() + 1);
        }

        // A ball is just detected in the pre-shooter stage,
        // at the top PE
        if (top)
        {
            ballIndexExitTimer.start();
        }

        // A ball has been in the intake entry PE for a sufficient
        // amount of time, and then passed it
        if (ballIndexExitTimer.hasElapsed(1.0) && !top)
        {
            ballExited = true;
            ballIndexExitTimer.reset();
            setIndexBallCount(getIndexBallCount() - 1);
        }

        // Error checking
        if(getIndexBallCount() < 0 || getIndexBallCount() > 2)
        {
            System.out.println("Index ball count error");
            // I did this so calling program could detect a
            // ball count error and act accordingly
            setIndexBallCount(-1);
        }
    }

    public int getIndexBallCount()
    {
        return ballsInIndex;
    }

    public void setIndexBallCount(int count)
    {
        ballsInIndex = count;
    }
    */
}
