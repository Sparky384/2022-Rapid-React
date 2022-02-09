package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;

public class Controller {
    XboxController controller = new XboxController(0);
    
    public double getRightY() {
        return controller.getRightY();
    }

    public double getLeftY() {
        return controller.getLeftY();
    }

    public double getLeftX() {
        return controller.getLeftX();
    }
    
    public boolean getXButton() {
        return controller.getXButton();
    }

    public boolean getAButton() {
        return controller.getAButton();
    }

    public boolean getYButton() {
        return controller.getYButton();
    }

    public boolean getBButton() {
        return controller.getBButton();
    }
    
    public boolean getLeftBumper(){
        return controller.getLeftBumper();
    }
    
    public boolean getRightBumper(){
        return controller.getRightBumper();
    }

    public double getRightX(){
        return controller.getRightX();
    }

    public boolean getRightTrigger(){
        if ((int) controller.getRightTriggerAxis() == 1)
            return true;
        else 
            return false;
    }

    public boolean getLeftTrigger(){
        if ((int) controller.getLeftTriggerAxis() == 1)
            return true;
        else 
            return false;
    }

}
