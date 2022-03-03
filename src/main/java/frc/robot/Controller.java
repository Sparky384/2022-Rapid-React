package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

public class Controller {
    XboxController pilot = new XboxController(0);
    XboxController copilot = new XboxController(1);

    public double getRightY(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getRightY();
        else
            return copilot.getRightY();
    }

    public double getLeftY(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getLeftY();
        else
            return copilot.getLeftY();
    }

    
    public double getRightX(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getRightX();
        else
            return copilot.getRightX();
    }

    public double getLeftX(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getLeftX();
        else
            return copilot.getLeftX();
    }
    
    public boolean getXButton(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getXButton();
        else
            return copilot.getXButton();
    }

    public boolean getAButton(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getAButton();
        else
            return copilot.getAButton();
    }

    public boolean getYButton(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getYButton();
        else
            return copilot.getYButton();
    }

    public boolean getBButton(int stick) 
    {
        if (stick == Constants.PILOT)
            return pilot.getBButton();
        else
            return copilot.getBButton();
    }
    
    public boolean getLeftBumper(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getLeftBumper();
        else
            return copilot.getLeftBumper();
    }
    
    public boolean getRightBumper(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getRightBumper();
        else
            return copilot.getRightBumper();
    }

    public boolean getRightTrigger(int stick)
    {
        if (stick == Constants.PILOT)
        {
            if (pilot.getRightTriggerAxis() > 0.5)
                return true;
            else 
                return false;
        }
        else
        {
            if (copilot.getRightTriggerAxis() > 0.5)
                return true;
            else 
                return false;
        }
    }

    public double getRightTriggerAxis(int stick)
    {
        if (stick == Constants.PILOT)
        {
            return pilot.getRightTriggerAxis();
        }
        else
        {
            return pilot.getRightTriggerAxis();
        }
    }

    public boolean getLeftTrigger(int stick)
    {
        if (stick == Constants.PILOT)
        {
            if (pilot.getLeftTriggerAxis() > 0.5)
                return true;
            else 
                return false;
        }
        else
        {
            if (copilot.getLeftTriggerAxis() > 0.5)
                return true;
            else 
                return false;
        }
    }

    public boolean getL3(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getLeftStickButton();
        else
            return copilot.getLeftStickButton();
    }

    public boolean getR3(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getRightStickButton();
        else
            return copilot.getRightStickButton();
    }

    public boolean getDpadUP(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getPOV() == 0;
        else
            return copilot.getPOV() == 0;
    }

    public boolean getDpadRIGHT(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getPOV() == 90;
        else
            return copilot.getPOV() == 90;
    }

    public boolean getDpadDOWN(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getPOV() == 180;
        else
            return copilot.getPOV() == 180;
    }

    public boolean getDpadLEFT(int stick)
    {
        if (stick == Constants.PILOT)
            return pilot.getPOV() == 270;
        else
            return copilot.getPOV() == 270;
    }


    public boolean getButton(int stick, Buttons button)
    {
        switch (button)
        {
        case Y:
            return getYButton(stick);
        case A:
            return getAButton(stick);
        case B:
            return getBButton(stick);
        case DDOWN:
            return getDpadDOWN(stick);
        case DLEFT:
            return getDpadLEFT(stick);
        case DRIGHT:
            return getDpadRIGHT(stick);
        case DUP:
            return getDpadUP(stick);
        case L3:
            return getL3(stick);
        case LB:
            return getLeftBumper(stick);
        case LT:
            return getLeftTrigger(stick);
        case R3:
            return getR3(stick);
        case RB:
            return getRightBumper(stick);
        case RT:
            return getRightTrigger(stick);
        case X:
            return getXButton(stick);
        default:
            return false;
        }
    }
}

