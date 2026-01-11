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
@TeleOp(name = "Red Test eleOp", group = "TeleOp")
public class RedTestTeleop extends OpMode {

    private Follower follower;
    public static Pose relocalizePose = new Pose(20.45698166431594, 11.424541607898442, Math.toRadians(180));

    private Intake intake;
    private Shooter shooter;
    private Kicker kicker;
    private Turret turret;
    private ScoringAction scoringAction;

    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;
    private double turningMultiplier = 0.4;

    // ========== CALIBRATION MODE ==========
    public static boolean calibrationMode = false;
    public static double testVelocity = 1000;

    // Goal positions
    private static final double RED_GOAL_X = 144;
    private static final double RED_GOAL_Y = 144;

    // Calibration data storage (up to 10 points)
    private static final int MAX_POINTS = 10;
    public static double[] distances = new double[MAX_POINTS];
    public static double[] velocities = new double[MAX_POINTS];
    private int currentPointIndex = 0;

    // Calculated values
    public static double calculatedSlope = 0;
    public static double calculatedIntercept = 0;
    public static double rSquared = 0;

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
        kicker.update();
        scoringAction.update();

        // Always-on turret auto-tracking (RED alliance)
        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                false  // false = RED alliance
        );
        turret.update();

        // ========== DRIVETRAIN CONTROL ==========
        double speedMultiplier = slowMode ? slowModeMultiplier : 1.0;

        follower.setTeleOpDrive(
                -gamepad1.left_stick_y * speedMultiplier,
                -gamepad1.left_stick_x * speedMultiplier,
                -gamepad1.right_stick_x * speedMultiplier * turningMultiplier,
                false //false = field oriented
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

        // ========== SHOOTER CONTROL ==========
        if (calibrationMode) {
            // CALIBRATION MODE: Use testVelocity from PANELS
            if (!scoringAction.isScoring()) {
                shooter.setVelocity(testVelocity);
            }
        } else {
            // NORMAL MODE: Use preset buttons
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

        // ========== CALIBRATION DATA COLLECTION ==========
        if (calibrationMode) {
            double currentDistance = Math.hypot(
                    RED_GOAL_X - follower.getPose().getX(),
                    RED_GOAL_Y - follower.getPose().getY()
            );

            // Save data point with DPAD_UP
            if (gamepad1.dpad_up && currentPointIndex < MAX_POINTS) {
                distances[currentPointIndex] = currentDistance;
                velocities[currentPointIndex] = testVelocity;
                currentPointIndex++;

                if (currentPointIndex >= 2) {
                    calculateSlopeIntercept();
                }
            }

            // Clear all data with DPAD_DOWN
            if (gamepad1.dpad_down) {
                for (int i = 0; i < MAX_POINTS; i++) {
                    distances[i] = 0;
                    velocities[i] = 0;
                }
                currentPointIndex = 0;
                calculatedSlope = 0;
                calculatedIntercept = 0;
                rSquared = 0;
            }

            // Remove last point with DPAD_LEFT
            if (gamepad1.dpad_left && currentPointIndex > 0) {
                currentPointIndex--;
                distances[currentPointIndex] = 0;
                velocities[currentPointIndex] = 0;

                if (currentPointIndex >= 2) {
                    calculateSlopeIntercept();
                } else {
                    calculatedSlope = 0;
                    calculatedIntercept = 0;
                    rSquared = 0;
                }
            }
        }

        // ========== TELEMETRY ==========
        if (calibrationMode) {
            // CALIBRATION MODE TELEMETRY
            double currentDistance = Math.hypot(
                    RED_GOAL_X - follower.getPose().getX(),
                    RED_GOAL_Y - follower.getPose().getY()
            );

            telemetry.addLine("=== CALIBRATION MODE ===");
            telemetry.addData("Distance", "%.1f in", currentDistance);
            telemetry.addData("Test Velocity", testVelocity);
            telemetry.addData("Actual Vel", "%.0f", shooter.getCurrentVelocity());
            telemetry.addData("Scoring", scoringAction.isScoring());
            telemetry.addLine();
            telemetry.addData("Points Saved", currentPointIndex + " / " + MAX_POINTS);
            for (int i = 0; i < Math.min(currentPointIndex, 5); i++) {
                telemetry.addData("  " + (i+1), "%.1f in → %.0f vel", distances[i], velocities[i]);
            }
            if (currentPointIndex > 5) {
                telemetry.addLine("  ... (" + (currentPointIndex - 5) + " more)");
            }
            telemetry.addLine();
            telemetry.addData("fSlope", "%.4f", calculatedSlope);
            telemetry.addData("fIntercept", "%.2f", calculatedIntercept);
            telemetry.addData("R²", "%.4f", rSquared);
            telemetry.addLine();
            telemetry.addLine("A: Test scoring");
            telemetry.addLine("DPAD_UP: Save point (if shot made it)");
            telemetry.addLine("DPAD_LEFT: Remove last");
            telemetry.addLine("DPAD_DOWN: Clear all");
        } else {
            // NORMAL MODE TELEMETRY
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
        }
        telemetry.update();
    }

    private void calculateSlopeIntercept() {
        int n = currentPointIndex;

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += distances[i];
            sumY += velocities[i];
            sumXY += distances[i] * velocities[i];
            sumX2 += distances[i] * distances[i];
        }

        calculatedSlope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        calculatedIntercept = (sumY - calculatedSlope * sumX) / n;

        // Calculate R²
        double meanY = sumY / n;
        double ssTotal = 0;
        double ssResidual = 0;

        for (int i = 0; i < n; i++) {
            double predicted = calculatedSlope * distances[i] + calculatedIntercept;
            ssTotal += Math.pow(velocities[i] - meanY, 2);
            ssResidual += Math.pow(velocities[i] - predicted, 2);
        }

        rSquared = 1 - (ssResidual / ssTotal);
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