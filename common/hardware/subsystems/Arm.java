package org.firstinspires.ftc.teamcode.common.hardware.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.controllers.Feedforward;
import org.firstinspires.ftc.teamcode.common.controllers.PIDF;
import org.firstinspires.ftc.teamcode.common.hardware.Global;
import org.firstinspires.ftc.teamcode.common.hardware.WRobot;
import org.firstinspires.ftc.teamcode.common.hardware.wrappers.WSubsystem;
import org.firstinspires.ftc.teamcode.common.util.WMath;

import java.util.function.DoubleSupplier;

public class Arm implements WSubsystem {
    private final WRobot robot = WRobot.getInstance();

    public enum ArmState { SCORING, FLAT }
    private ArmState arm_state = ArmState.FLAT;

    public DoubleSupplier arm_angle;
    public double target_position;
    public double increment = 0;
    public double power = 0.0;

    //controllers
    private final PIDF arm_controller = new PIDF(0.0012, 0.0001, 0.0002, 0.65, 2000.0, 10.0);
    private final Feedforward arm_support = new Feedforward(0.07);

    public Arm() {

    }

    public void init (DcMotorEx lift) {
        lift.setDirection(DcMotorSimple.Direction.FORWARD);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        arm_angle = () -> robot.arm_actuator.getCurrentPosition() / (3 * Global.TETRIX_MOTOR_TPR) * 2 * Math.PI;
        target_position = robot.arm_actuator.getCurrentPosition();
    }

    public void periodic() {
        if (Global.IS_AUTO) {

        } else {
            switch (arm_state) {
                case FLAT:
                    target_position = robot.arm_actuator.getReadingOffset() + increment;
                    break;

                case SCORING:
                    target_position = (double) Global.TETRIX_MOTOR_TPR + increment;
            }
        }

        power = arm_controller.calculate(robot.arm_actuator.getCurrentPosition(), target_position) +
                (arm_support.calculate(Math.cos(arm_angle.getAsDouble())) * ((arm_state == ArmState.FLAT) ? 0 : 1));
        robot.arm_actuator.setPower(power);
    }

    public void read() {
        robot.arm_actuator.read();
    }

    public void write() {
        robot.arm_actuator.write();
    }

    public void reset() {
        arm_controller.reset(0);
        setArmState(ArmState.FLAT);
    }

    public ArmState getArmState() {
        return arm_state;
    }

    public void setArmState(ArmState state) {
        arm_state = state;
    }

    public void setTargetPosition (double target_position){
        this.target_position = target_position;
    }

    public void incrementHeight(double increment) {
        this.increment -= increment;
        this.increment = WMath.clamp(this.increment, 0, 800);
    }

    public void resetIncrement() {
        this.increment = 0;
    }
}