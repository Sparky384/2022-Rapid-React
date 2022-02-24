package frc.robot;

public class Constants {
    //driveTo method constants
    public static double driveP = 0.02896;
    public static double driveI = 1.55;
    public static double deadBand = 10;
    public static double turnP = 0.009300; //0.0245  
    public static double turnI = 0.000001; //0.00375
    public static double turnD = 0.089000; //0.0
    public static double turnDeadBand = 3; //10
    public static double shooterP = 0.001143;
    public static double shooterI = 0.00006;
    public static double shooterD = 0.0045;
    public static double shooterMaxI = 400.0;
    public static double shooterIzone = 500.0;
    public static double shooterThreshold = 400.0;
    public static double centerDeadBand = 2.5;
    public static double centerIntervalTime = 1.0;

    //ports for 2020 robot
    public static int stage1MotorPort = 4;  //intake motor
    public static int stage2MotorPort = 5;  //intake motor
    public static int stage3MotorPort = 11;  //intake motor
    public static int bottomPhotoEyePort = 0;
    public static int topPhotoEyePort = 2;
    public static int frontLeftPort = 1;  //drivetrain motors
    public static int frontRightPort = 14;  //drivetrain motors
    public static int backLeftPort = 16;  //drivetrain motors
    public static int backRightPort = 15;  //drivetrain motors
    public static int shooterMotorRightPort = 13;
    public static int shooterMotorLeftPort = 2;
    public static int shooterMotorTurnPort = 10; //not going to be used on the actual robot
   
    //camera
    public static final double targetHeight = 93.0;
    public static final double cameraHeight = 40.0;
    public static final double cameraAngle = Math.toRadians(0.0);
   
    //autono-moose
    public static final int TURN_AND_DRIVE= 0;
    public static final int DRIVE_AND_TURN = 1;
    public static final int DO_NOTHING = 2;
    public static final int TEST = 3;
    public static final int THREE_BALL_AUTO = 4;
    public static final int TWO_BALL_AUTO = 5;
    public static final int ONE_BALL_AUTO = 6;
    public static final int ZERO_BALL_AUTO = 7;
    public static final int TURN = 8;

    public static final int PILOT = 0;
    public static final int COPILOT = 1;

    //

    public static final int proto = 0;
    public static final int sparky = 1;

    Constants(int bot)
    {
        if (bot == proto) // proto
        {
            stage1MotorPort = 4;  //intake motor
            stage2MotorPort = 5;  //intake motor
            stage3MotorPort = 11;  //intake motor
            bottomPhotoEyePort = 0;
            topPhotoEyePort = 2;
            frontLeftPort = 1;  //drivetrain motors
            frontRightPort = 14;  //drivetrain motors
            backLeftPort = 16;  //drivetrain motors
            backRightPort = 15;  //drivetrain motors
            shooterMotorRightPort = 13;
            shooterMotorLeftPort = 2;
            shooterMotorTurnPort = 10; //not going to be used on the actual robot

            driveP = 0.02896;
            driveI = 1.55;
            deadBand = 10;
            turnP = 0.009300; //0.0245  
            turnI = 0.000001; //0.00375
            turnD = 0.089000; //0.0
            turnDeadBand = 3; //10
            shooterP = 0.001143;
            shooterI = 0.00006;
            shooterD = 0.0045;
            shooterMaxI = 400.0;
            shooterIzone = 500.0;
            shooterThreshold = 400.0;
            centerDeadBand = 2.5;
            centerIntervalTime = 1.0;
        }
        else // final
        {
            stage1MotorPort = 4;  //intake motor
            stage2MotorPort = 5;  //intake motor
            stage3MotorPort = 11;  //intake motor
            bottomPhotoEyePort = 0;
            topPhotoEyePort = 2;
            frontLeftPort = 1;  //drivetrain motors
            frontRightPort = 14;  //drivetrain motors
            backLeftPort = 16;  //drivetrain motors
            backRightPort = 15;  //drivetrain motors
            shooterMotorRightPort = 13;
            shooterMotorLeftPort = 2;

            driveP = 0.02896;
            driveI = 1.55;
            deadBand = 10;
            turnP = 0.009300; //0.0245  
            turnI = 0.000001; //0.00375
            turnD = 0.089000; //0.0
            turnDeadBand = 3; //10
            shooterP = 0.001143;
            shooterI = 0.00006;
            shooterD = 0.0045;
            shooterMaxI = 400.0;
            shooterIzone = 500.0;
            shooterThreshold = 400.0;
            centerDeadBand = 2.5;
            centerIntervalTime = 1.0;
        }
    }
}
