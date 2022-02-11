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

}
