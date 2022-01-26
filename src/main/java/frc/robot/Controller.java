package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

public class Controller {
    XboxController controller = new XboxController(1);
    
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

    public boolean getBButton() {
        return controller.getBButton();
    }
}
