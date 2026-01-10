package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.autos.BlueClose12Piece.autoEndPose;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Kicker;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.subsystems.ScoringAction;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Configurable
@TeleOp(name = "Blue TeleOp", group = "TeleOp")
public class BlueTeleop extends OpMode {

    private Follower follower;
    public static Pose relocalizePose = new Pose(123.54301833568406, 11.424541607898442, Math.toRadians(0)); // Mirrored from red

    private Intake intake;
    private Shooter shooter;
    private Kicker kicker;
    private Turret turret;
    private ScoringAction scoringAction;

    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;
    private double turningMultiplier = 0.4;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(autoEndPose == null ? new Pose() : autoEndPose);
        follower.update();

        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        kicker = new Kicker(hardwareMap);
        turret = new Turret(hardwareMap);
        turret.resetEncoder();

        scoringAction = new ScoringAction(intake, kicker);

        telemetry.addLine("Blue TeleOp Initialized!");
        telemetry.update();
    }

    @Override
    public void start() {
        follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        // ========== UPDATE ALL SYSTEMS ==========
        follower.update();
        kicker.update();
        scoringAction.update();

        // Always-on turret auto-tracking (BLUE alliance)
        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                true  // true = BLUE alliance
        );
        turret.update();

        // ========== DRIVETRAIN CONTROL ==========
        double speedMultiplier = slowMode ? slowModeMultiplier : 1.0;

        follower.setTeleOpDrive(
                -gamepad1.left_stick_y * speedMultiplier,
                -gamepad1.left_stick_x * speedMultiplier,
                -gamepad1.right_stick_x * speedMultiplier * turningMultiplier,
                false
        );

        // ========== SLOW MODE ==========
        if (gamepad1.rightStickButtonWasPressed()) {
            slowMode = !slowMode;
        }

        // ========== RELOCALIZATION ==========
        // Back button = Reset pose to known position for relocalization
        if (gamepad1.backWasPressed()) {
            follower.setPose(relocalizePose);
        }

        // ========== INTAKE CONTROL (only if not scoring) ==========
        if (!scoringAction.isScoring()) {
            if (gamepad1.right_trigger > 0.5) {
                intake.startIntake();
            }
            else if (gamepad1.left_trigger > 0.5) {
                intake.startOuttake();
            }
            else {
                intake.stop();
            }
        }

        // ========== SHOOTER PRESET CONTROL ==========
        if (gamepad1.rightBumperWasPressed()) {
            if (shooter.getCurrentShotMode() == Shooter.ShotMode.NEAR) {
                shooter.turnOff();
            } else {
                shooter.setNearShot();
            }
        }

        if (gamepad1.leftBumperWasPressed()) {
            if (shooter.getCurrentShotMode() == Shooter.ShotMode.FAR) {
                shooter.turnOff();
            } else {
                shooter.setFarShot();
            }
        }

        if (gamepad1.yWasPressed()) {
            shooter.turnOff();
        }

        // ========== SCORING SEQUENCE ==========
        if (gamepad1.aWasPressed() && !scoringAction.isScoring()) {
            scoringAction.startScoring();
        }

        if (gamepad1.startWasPressed() && scoringAction.isScoring()) {
            scoringAction.stopScoring();
        }

        // ========== MANUAL KICKER CONTROL (only if not scoring) ==========
        if (!scoringAction.isScoring()) {
            if (gamepad1.bWasPressed()) {
                kicker.extend();
            }

            if (gamepad1.xWasPressed()) {
                kicker.retract();
            }
        }

        // ========== TELEMETRY ==========
        telemetry.addData("Slow Mode", slowMode);
        telemetry.addData("Scoring", scoringAction.isScoring());
        telemetry.addData("Scoring State", scoringAction.getCurrentState());
        telemetry.addData("Intake", intake.getState());
        telemetry.addData("Shooter Mode", shooter.getCurrentShotMode());
        telemetry.addData("Shooter Vel", "%.0f", shooter.getCurrentVelocity());
        telemetry.addData("Kicker", kicker.getCurrentState());
        telemetry.addData("Turret Angle", "%.1f°", turret.getCurrentAngle());
        telemetry.addData("Turret Target", "%.1f°", turret.getTargetAngle());
        telemetry.addData("Robot X", follower.getPose().getX());
        telemetry.addData("Robot Y", follower.getPose().getY());
        telemetry.addData("Heading", "%.1f°", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.update();
    }

    @Override
    public void stop() {
        scoringAction.stopScoring();
        intake.stop();
        shooter.turnOff();
        kicker.retract();
        turret.stop();
        autoEndPose = follower.getPose();
    }
}