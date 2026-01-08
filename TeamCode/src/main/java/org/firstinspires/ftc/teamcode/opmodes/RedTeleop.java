package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.autos.RedClose12Piece.autoEndPose;

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
@TeleOp(name = "Red TeleOp", group = "TeleOp")
public class RedTeleop extends OpMode {

    private Follower follower;
    public static Pose resetPose = new Pose(72, 72, Math.toRadians(90));

    private Intake intake;
    private Shooter shooter;
    private Kicker kicker;
    private Turret turret;
    private ScoringAction scoringAction;

    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;
    private double turningMultiplier = 0.6;

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

        // Initialize scoring action
        scoringAction = new ScoringAction(intake, kicker);

        telemetry.addLine("Red TeleOp Initialized!");
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
        kicker.update(); // CRITICAL: Updates automatic pulse timing
        scoringAction.update(); // CRITICAL: Update scoring state machine every loop

        // TURRET COMMENTED OUT FOR TESTING
        /*
        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                false  // false = RED alliance
        );
        turret.update();
        */

        // ========== DRIVETRAIN CONTROL ==========
        double speedMultiplier = slowMode ? slowModeMultiplier : 1.0;

        follower.setTeleOpDrive(
                -gamepad1.left_stick_y * speedMultiplier,
                -gamepad1.left_stick_x * speedMultiplier,
                -gamepad1.right_stick_x * speedMultiplier * turningMultiplier,
                false  // false = field-oriented
        );

        // ========== SLOW MODE ==========
        if (gamepad1.rightStickButtonWasPressed()) {
            slowMode = !slowMode;
        }

        // ========== RESET POSE ==========
        if (gamepad1.leftStickButtonWasPressed()) {
            follower.setPose(resetPose);
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
        // Right bumper = Near shot (toggle on/off)
        if (gamepad1.rightBumperWasPressed()) {
            if (shooter.getCurrentShotMode() == Shooter.ShotMode.NEAR) {
                shooter.turnOff();
            } else {
                shooter.setNearShot();
            }
        }

        // Left bumper = Far shot (toggle on/off)
        if (gamepad1.leftBumperWasPressed()) {
            if (shooter.getCurrentShotMode() == Shooter.ShotMode.FAR) {
                shooter.turnOff();
            } else {
                shooter.setFarShot();
            }
        }

        // Y button = Stop shooter
        if (gamepad1.yWasPressed()) {
            shooter.turnOff();
        }

        // ========== SCORING SEQUENCE ==========
        // A button = Start automatic scoring sequence
        if (gamepad1.aWasPressed() && !scoringAction.isScoring()) {
            scoringAction.startScoring();
        }

        // Start button = Emergency stop scoring sequence
        if (gamepad1.startWasPressed() && scoringAction.isScoring()) {
            scoringAction.stopScoring();
        }

        // ========== MANUAL KICKER CONTROL (only if not scoring) ==========
        if (!scoringAction.isScoring()) {
            // B button = Manual extend
            if (gamepad1.bWasPressed()) {
                kicker.extend();
            }

            // X button = Manual retract
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
        // telemetry.addData("Turret Angle", "%.1f°", turret.getCurrentAngle());
        // telemetry.addData("Turret Target", "%.1f°", turret.getTargetAngle());
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