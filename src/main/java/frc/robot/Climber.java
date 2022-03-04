package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

public class Climber {
    private WPI_TalonFX climberMotor;

    public Climber()
    {
        climberMotor = new WPI_TalonFX(Constants.climberMotorPort); 
        climberMotor.setNeutralMode(NeutralMode.Brake);
    }

    public void move(double Yaxis)
    {
        if (climberMotor.getSelectedSensorPosition() < Constants.climberMinimumPosition && Yaxis < 0)
            climberMotor.set(0);
        else
            climberMotor.set(Yaxis);
    }
}
