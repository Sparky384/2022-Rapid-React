package frc.robot;

import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight {

    private static boolean lightOn = false; 
    private static boolean visionMode = true;

    public static double getTargetAngleXOffset(String cam)
    {
      double xOffset = NetworkTableInstance.getDefault().getTable(cam).getEntry("tx").getDouble(0);
      //Logging.consoleLog("CamXOffset: " + offset); 
      return xOffset;
      //the center of the camera has xOffset equal 0
      //if the target is right of the crosshair, xOffset is positive
    }

    public static double getTargetAngleYOffset(String cam)
    {
        double yOffset = NetworkTableInstance.getDefault().getTable(cam).getEntry("ty").getDouble(0);
        return yOffset;
    }

    public static double getTargetArea(String cam)
    {
        double area = NetworkTableInstance.getDefault().getTable(cam).getEntry("ta").getDouble(0);
        return area;
    }

    public static double getValidTargets(String cam)
    {
        return NetworkTableInstance.getDefault().getTable(cam).getEntry("tv").getDouble(0);
    }

    public static double calculateDistance(String cam)
    {
        double angle = Math.toRadians(getTargetAngleYOffset(cam)) + Constants.cameraAngle;
        double heightDiff = Constants.targetHeight - Constants.cameraHeight;
        double distance = heightDiff / Math.tan(angle);
        // This offset brought the raw versus measured curves together
        distance -= 30;
        return distance; 
    }

    public static void toggleLight(String cam)
    {
      if(!lightOn)
      {
        NetworkTableInstance.getDefault().getTable(cam).getEntry("ledMode").setNumber(3);
        lightOn = true;
      } 
      else if (lightOn)
      {
        NetworkTableInstance.getDefault().getTable(cam).getEntry("ledMode").setNumber(1);
        lightOn = false;
      }
    }

    // Turns the light on
    public static void lightOn(String cam)
    {
      NetworkTableInstance.getDefault().getTable(cam).getEntry("ledMode").setNumber(3);
    }

    // Turns the light off
    public static void lightOff(String cam)
    {
      NetworkTableInstance.getDefault().getTable(cam).getEntry("ledMode").setNumber(1);
    }

    public static void toggleCamMode(String cam)
    {
        if(!visionMode)
        {
            NetworkTableInstance.getDefault().getTable(cam).getEntry("camMode").setNumber(0);
            visionMode = true;
        }
        else if (visionMode)
        {
          NetworkTableInstance.getDefault().getTable(cam).getEntry("camMode").setNumber(1);
          visionMode = false;
        }
    }

    public static boolean getLight(String cam) 
    {
      return lightOn;
    }
}