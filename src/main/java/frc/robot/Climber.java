package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

public class Climber {
    private WPI_TalonFX climberMotor;

    public Climber()
    {
        climberMotor = new WPI_TalonFX(Constants.climberMotorPort); 
        climberMotor.setNeutralMode(NeutralMode.Brake);
        climberMotor.setSelectedSensorPosition(0.0);
    }

    public void move(double Yaxis)
    {
        Yaxis *= -1;
        //System.out.println(Yaxis);
        //System.out.println(climberMotor.getSelectedSensorPosition());
        //System.out.println("----");
        if (Yaxis < 0 && climberMotor.getSelectedSensorPosition() <= Constants.climberMinimumPosition)
            climberMotor.set(0);
        if (Yaxis > 0 && climberMotor.getSelectedSensorPosition() >= Constants.climberMaximumPosition)
            climberMotor.set(0);
        else
        {
            if (Yaxis > 0)
                climberMotor.set(Yaxis);
            else
                climberMotor.set(Yaxis * 0.5);
        }
    }
}
