package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.autos.RedClose12Piece.autoEndPose;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Configurable
@TeleOp(name = "Red TeleOp", group = "TeleOp")
public class RedTeleop extends OpMode {

    private Follower follower;
    public static Pose resetPose = new Pose(72, 72, Math.toRadians(90));

    private Intake intake;
    private Turret turret;

    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;
    private double turningMultiplier = 0.6;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(autoEndPose == null ? new Pose() : autoEndPose);
        follower.update();

        intake = new Intake(hardwareMap);
        turret = new Turret(hardwareMap);
        turret.resetEncoder();

        telemetry.addLine("Test TeleOp Initialized!");
        telemetry.update();
    }

    @Override
    public void start() {
        follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        follower.update();

        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                false  // false = red alliance
        );
        turret.update();

        double speedMultiplier = slowMode ? slowModeMultiplier : 1.0;

        follower.setTeleOpDrive(
                -gamepad1.left_stick_y * speedMultiplier,
                -gamepad1.left_stick_x * speedMultiplier,
                -gamepad1.right_stick_x * speedMultiplier * turningMultiplier,
                false  // false = field-oriented
        );

        if (gamepad1.right_stick_button) {
            slowMode = !slowMode;
        }

        if (gamepad1.left_stick_button) {
            follower.setPose(resetPose);
        }

        if (gamepad1.right_trigger > 0.5) {
            intake.startIntake();
        }
        else if (gamepad1.left_trigger > 0.5) {
            intake.startOuttake();
        }
        else {
            intake.stop();
        }

        telemetry.addData("Slow Mode", slowMode);
        telemetry.addData("Intake State", intake.getState());
        telemetry.addData("Turret Angle", "%.1f°", turret.getCurrentAngle());
        telemetry.addData("Turret Target", "%.1f°", turret.getTargetAngle());
        telemetry.addData("At Target", turret.atTarget());
        telemetry.addData("Robot X", follower.getPose().getX());
        telemetry.addData("Robot Y", follower.getPose().getY());
        telemetry.addData("Robot Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.update();
    }

    @Override
    public void stop() {
        intake.stop();
        turret.stop();
        autoEndPose = follower.getPose();
    }
}