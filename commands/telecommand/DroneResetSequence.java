package org.firstinspires.ftc.teamcode.commands.telecommand;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.commands.subsystemcommand.arm.ArmResetIncrementCommand;
import org.firstinspires.ftc.teamcode.commands.subsystemcommand.arm.ArmSetStateCommand;
import org.firstinspires.ftc.teamcode.commands.subsystemcommand.intake.ClawCommand;
import org.firstinspires.ftc.teamcode.commands.subsystemcommand.intake.WristCommand;
import org.firstinspires.ftc.teamcode.common.hardware.Global;
import org.firstinspires.ftc.teamcode.common.hardware.subsystems.Arm;
import org.firstinspires.ftc.teamcode.common.hardware.subsystems.Intake;

public class DroneResetSequence extends SequentialCommandGroup {
    public DroneResetSequence() {
        super(
                new InstantCommand(() -> Global.setState(Global.State.INTERMEDIATE)),
                new WristCommand(Intake.WristState.FOLD),
                new WaitCommand(1000),
                new ArmSetStateCommand(Arm.ArmState.FLAT),
                new ArmResetIncrementCommand(),
                new WaitCommand(300),
                new ClawCommand(Intake.ClawSide.BOTH, Intake.ClawState.CLOSED)
        );
    }
}
