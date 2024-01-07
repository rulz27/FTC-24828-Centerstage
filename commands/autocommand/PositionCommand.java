package org.firstinspires.ftc.teamcode.commands.autocommand;


import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.external.samples.RobotHardware;
import org.firstinspires.ftc.teamcode.common.controllers.PIDF;
import org.firstinspires.ftc.teamcode.common.hardware.WRobot;
import org.firstinspires.ftc.teamcode.common.hardware.drive.Drivetrain;
import org.firstinspires.ftc.teamcode.common.hardware.drive.pathing.Localizer;
import org.firstinspires.ftc.teamcode.common.util.Pose;
import org.firstinspires.ftc.teamcode.common.util.Vector2D;
import org.firstinspires.ftc.teamcode.common.util.WMath;

public class PositionCommand extends CommandBase {
    private final WRobot robot = WRobot.getInstance();

    private final Drivetrain drivetrain = robot.drivetrain;
    private final Localizer localizer = robot.localizer;
    private final Pose target_pose;

    public static double xP = 0.105;
    public static double xD = 0.0175;

    public static double yP = 0.105;
    public static double yD = 0.0175;

    public static double zP = 1.5;
    public static double zD = 0.075;

    public static PIDF xController = new PIDF(xP, 0.0, xD);
    public static PIDF yController = new PIDF(yP, 0.0, yD);
    public static PIDF zController = new PIDF(zP, 0.0, zD);

    public static double TRANSLATIONAL_TOLERANCE = 2;
    public static double YAW_TOLERANCE = 0.03;

    private ElapsedTime timer;
    private ElapsedTime stable;

    public static double DEAD_MS = 5000;
    public static double STABLE_MS = 400;

    public PositionCommand(Pose pose) {
        target_pose = pose;

        xController.reset();
        yController.reset();
        zController.reset();
    }

    @Override
    public void execute() {
        if (timer == null) timer = new ElapsedTime();
        if (stable == null) stable = new ElapsedTime();

        Pose robot_pose = localizer.getPose();

        Pose power_matrix = calculatePower(robot_pose);
        drivetrain.move(power_matrix);
    }

    @Override
    public boolean isFinished() {
        Pose robot_pose = localizer.getPose();
        Pose delta = target_pose.subtract(robot_pose);

        if (delta.toVector2D().magnitude() > TRANSLATIONAL_TOLERANCE
                || Math.abs(delta.z) > YAW_TOLERANCE) {
            stable.reset();
        }

        return timer.milliseconds() > DEAD_MS || stable.milliseconds() > STABLE_MS;
    }

    public Pose calculatePower(Pose robot_pose) {
        double xPower = xController.calculate(robot_pose.x, target_pose.x);
        double yPower = yController.calculate(robot_pose.y, target_pose.y);
        double zPower = -zController.calculate(robot_pose.z, target_pose.z);

        Vector2D translation_vector = new Vector2D(xPower, yPower, robot_pose.z);
        zPower = WMath.clamp(zPower, -1.0, 1.0);        //TODO: tune this

        return new Pose(translation_vector, zPower);
    }

    @Override
    public void end(boolean interrupted) {
        drivetrain.move(new Pose());
    }
}
