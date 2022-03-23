package frc.robot;

import edu.wpi.first.networktables.NetworkTableInstance;

// This should be the limelight_balltrack branch
public class Limelight {

    private static boolean lightOn = false; 
    private static boolean visionMode = true;

    public static double getTargetAngleXOffset()
    {
      double xOffset = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
      //Logging.consoleLog("CamXOffset: " + offset); 
      return xOffset;
      //the center of the camera has xOffset equal 0
      //if the target is right of the crosshair, xOffset is positive
    }

    public static double getTargetAngleYOffset()
    {
        double yOffset = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
        return yOffset;
    }

    public static double getTargetArea()
    {
        double area = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);
        return area;
    }

    public static double getValidTargets()
    {
        return NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
    }

    public static double calculateDistance()
    {
        double angle = Math.toRadians(getTargetAngleYOffset()) + Constants.cameraAngle;
        double heightDiff = Constants.targetHeight - Constants.cameraHeight;
        double distance = heightDiff / Math.tan(angle);
        return distance;
    }

    public static void toggleLight()
    {
      if(!lightOn)
      {
        NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(3);
        lightOn = true;
      } 
      else if (lightOn)
      {
        NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
        lightOn = false;
      }
    }

    // Turns the light on
    public static void lightOn()
    {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(3);
    }

    // Turns the light off
    public static void lightOff()
    {
      NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
    }

    public static void toggleCamMode()
    {
        if(!visionMode)
        {
            NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(0);
            visionMode = true;
        }
        else if (visionMode)
        {
          NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode").setNumber(1);
          visionMode = false;
        }
    }

    public static boolean getLight() 
    {
      return lightOn;
    }
}