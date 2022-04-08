package frc.robot;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight {

    private static boolean lightOn = false; 
    private static boolean visionMode = true;
    private static ArrayList<Boolean> alive = new ArrayList<>();

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

        // Uncomment for snapshot mode
        //boolean snapShotTaken = false;

        double angle = Math.toRadians(getTargetAngleYOffset(cam) + Constants.cameraAngle);
        double heightDiff = Constants.targetHeight - Constants.cameraHeight; //71.74
        // Angle must be in radians
        double distance = heightDiff / Math.tan(angle);
        // This offset brought the raw versus measured curves together
        distance -= 22;

        // Take a snapshot
        takeSnapShot(Constants.GOAL);

        return distance; 
    }

    public static boolean isLimelightAlive()
    {
      try {
      InetAddress cam = InetAddress.getByName("10.3.84.11");
      alive.add(cam.isReachable(4)); // 4 ms timeout
      if (alive.size() > 3)
        alive.remove(0);
      if (alive.size() == 3)
        return !alive.get(0) && !alive.get(1) && !alive.get(2); // not alive if 3 failed pings
      else
        return true; // always return true if less than 3 values
      } catch (Exception e) {
        return false;
      }
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

    public static void setCamMode(String cam, int value)
    {
      NetworkTableInstance.getDefault().getTable(cam).getEntry("camMode").setNumber(value);
    }

    public static boolean getLight(String cam) 
    {
      return lightOn;
    }

    public static void takeSnapShot(String cam) 
    {
      NetworkTableInstance.getDefault().getTable(cam).getEntry("snapshot").setNumber(1);
    }

    public static void resetSnapShot(String cam) 
    {
      NetworkTableInstance.getDefault().getTable(cam).getEntry("snapshot").setNumber(0);
    }

}