package org.firstinspires.ftc.teamcode.opmode.auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.commands.autocommand.FirstStackSetup;
import org.firstinspires.ftc.teamcode.commands.autocommand.PositionCommand;
import org.firstinspires.ftc.teamcode.commands.autocommand.PurplePixelSequence;
import org.firstinspires.ftc.teamcode.commands.autocommand.YellowPixelSequence;
import org.firstinspires.ftc.teamcode.commands.autocommand.FirstStackDeposit;
import org.firstinspires.ftc.teamcode.commands.autocommand.FirstStackGrabCommand;
import org.firstinspires.ftc.teamcode.common.hardware.Global;
import org.firstinspires.ftc.teamcode.common.hardware.Sensors;
import org.firstinspires.ftc.teamcode.common.hardware.WRobot;
import org.firstinspires.ftc.teamcode.common.hardware.drive.Drivetrain;
import org.firstinspires.ftc.teamcode.common.hardware.subsystems.Arm;
import org.firstinspires.ftc.teamcode.common.hardware.subsystems.Intake;
import org.firstinspires.ftc.teamcode.common.hardware.drive.pathing.Pose;

@Autonomous(name = "Red Front Auto")
public class RedFrontAuto extends CommandOpMode {

    private final WRobot robot = WRobot.getInstance();

    private double loop_time = 0.0;
    private final ElapsedTime timer = new ElapsedTime();
    private double end_time = 0;

    @Override
    public void initialize() {
        CommandScheduler.getInstance().reset();

        Global.IS_AUTO = true;
        Global.USING_IMU = true;
        Global.USING_DASHBOARD = false;
        Global.USING_WEBCAM = false;
        Global.DEBUG = false;
        Global.SIDE = Global.Side.RED;

        robot.addSubsystem(new Drivetrain(), new Arm(), new Intake());
        robot.init(hardwareMap, telemetry);

        robot.arm.setArmState(Arm.ArmState.FLAT);
        robot.intake.setWristState(Intake.WristState.FOLD);
        robot.intake.setClawState(Intake.ClawSide.BOTH, Intake.ClawState.CLOSED);

        if (Global.USING_DASHBOARD) {
            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
            FtcDashboard.getInstance().startCameraStream(robot.pipeline, 0);
        }

        robot.localizer.reset(new Pose(0, 0, -Math.PI));
        robot.read();

//        while (robot.vision_portal.getCameraState() != VisionPortal.CameraState.STREAMING && robot.pipeline.getPropLocation() == null) {
//            telemetry.addLine("Autonomous initializing...");
//            telemetry.update();
//        }

        while (!isStarted()) {
//            telemetry.addData("Path:", robot.pipeline.getPropLocation());
            telemetry.addData("Pose", robot.localizer.getPose().toString());
            telemetry.addData("Encoder readings", "%.2f, %.2f, %.2f",
                    robot.encoder_readings.get(Sensors.Encoder.POD_LEFT),
                    robot.encoder_readings.get(Sensors.Encoder.POD_MIDDLE),
                    robot.encoder_readings.get(Sensors.Encoder.POD_RIGHT));
            telemetry.addLine("Ready");
            telemetry.update();
        }

        robot.resetYaw();

        Pose purple_pose;
        Pose yellow_pose;
        Pose left_spike = new Pose();

        Global.PropLocation chosen = Global.PropLocation.RIGHT;

        switch (chosen) {
            case LEFT:
                purple_pose = new Pose(0, 27, Math.PI/2);
                left_spike = purple_pose;
                yellow_pose = new Pose(29, 32, 1.4);
                break;
            case  CENTER:
                purple_pose = new Pose(17, 38, Math.PI/2);
                left_spike = purple_pose;
                yellow_pose = new Pose(29, 27, 1.35);
                break;
            default:
                purple_pose = new Pose(22, 25, Math.PI/2);
                left_spike = purple_pose;
                yellow_pose = new Pose(29, 19, Math.PI/2);
                break;
        }

        Pose first_stack_pose = new Pose(-96, 49, -Math.PI / 2);
        Pose first_stack_deposit = new Pose(27, 25, -Math.PI / 2);

        CommandScheduler.getInstance().schedule(
                new SequentialCommandGroup(
                        new InstantCommand(timer::reset),

                        //purple deposit
                        new PositionCommand(purple_pose)
                                .andThen(new PositionCommand(left_spike))
                                .andThen(new PurplePixelSequence()),

                        //yellow deposit
                        new PositionCommand(yellow_pose)
                                .andThen(new YellowPixelSequence()),

                        //go to first stack
//                        new PositionCommand(new Pose(25, 52, -Math.PI / 2))
//                        .andThen(new PositionCommand(first_stack_pose))
//                        .alongWith(new FirstStackSetup()),

//                        new FirstStackGrabCommand(),
//                        new WaitCommand(500),
//
//                        new PositionCommand(new Pose(-20, 60, -Math.PI / 2)),
//
//                        new PositionCommand(first_stack_deposit)
//                                .andThen(new FirstStackDeposit()),

                        new PositionCommand(new Pose(35, 3, Math.PI / 2)),

                        new InstantCommand(() -> end_time = timer.seconds())

                )
        );

//        robot.vision_portal.setProcessorEnabled(robot.pipeline, false); //deallocate cpu resources
//        robot.vision_portal.close();
    }


    @Override
    public void run() {
        robot.read();
        super.run();
        robot.periodic();
        robot.write();
        robot.clearBulkCache(Global.Hub.BOTH);

        double loop = System.nanoTime();
        telemetry.addData("Frequency", "%3.2fhz", 1000000000 / (loop - loop_time));
        telemetry.addData("Voltage", robot.getVoltage());
        telemetry.addData("Pose", robot.localizer.getPose().toString());
        telemetry.addData("Runtime:", "%.2f", end_time == 0 ? timer.seconds() : end_time);

        if (Global.DEBUG) {
            telemetry.addLine("---------------------------");
            telemetry.addData("arm target", robot.arm.target_position);
            telemetry.addData("arm power", robot.arm.power);
            telemetry.addData("arm state", robot.arm.getArmState());

            telemetry.addLine("---------------------------");
            telemetry.addData("wrist target", robot.intake.target_position);
            telemetry.addData("wrist angle", robot.intake.wrist_angle);
        }

        loop_time = loop;
        telemetry.update();
    }

    @Override
    public void reset() {
        super.reset();
        robot.reset();
        Global.resetGlobals();
//        robot.updateYaw();
        Global.YAW_OFFSET = robot.getYaw();
    }
}
