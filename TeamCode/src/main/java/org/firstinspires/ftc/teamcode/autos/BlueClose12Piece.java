package org.firstinspires.ftc.teamcode.autos;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.Kicker;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.subsystems.ScoringAction;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Configurable
@Autonomous(name = "Blue Close 12 Piece", group = "Autonomous")
public class BlueClose12Piece extends OpMode {

    private TelemetryManager panelsTelemetry;
    public Follower follower;
    private int pathState;
    private Paths paths;
    private ElapsedTime shooterTimer;
    private ElapsedTime waitTimer;

    // Subsystems
    private Intake intake;
    private Shooter shooter;
    private Kicker kicker;
    private Turret turret;
    private ScoringAction scoringAction;

    // Starting pose (mirrored from red)
    private static final Pose startPose = new Pose(21, 122, Math.toRadians(139.5));

    // Timing constants
    private static final double SHOOTER_SPINUP_TIME = 0.25;
    private static final double POST_SCORE_WAIT = 0.4;

    // Store end pose for teleop continuity
    public static Pose autoEndPose = null;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        // Initialize Pedro Pathing
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startPose);

        // Initialize timers
        shooterTimer = new ElapsedTime();
        waitTimer = new ElapsedTime();

        // Initialize subsystems
        intake = new Intake(hardwareMap);
        shooter = new Shooter(hardwareMap);
        kicker = new Kicker(hardwareMap);
        turret = new Turret(hardwareMap);
        turret.resetEncoder();
        scoringAction = new ScoringAction(intake, kicker);

        // Build paths
        paths = new Paths(follower);

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        shooter.setNearShot(); // Turn on shooter at near shot velocity
        shooterTimer.reset();
        pathState = 0;
    }

    @Override
    public void loop() {
        follower.update();
        scoringAction.update();
        kicker.update();

        // Always-on turret auto-tracking (BLUE alliance)
        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                true  // true = BLUE alliance
        );
        turret.update();

        pathState = autonomousPathUpdate();

        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("Scoring State", scoringAction.getCurrentState());
        panelsTelemetry.debug("Shooter Velocity", shooter.getCurrentVelocity());
        panelsTelemetry.debug("Turret Angle", turret.getCurrentAngle());
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.update(telemetry);
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 0: // Wait for shooter to spin up
                if (shooterTimer.seconds() >= SHOOTER_SPINUP_TIME) {
                    follower.followPath(paths.ScorePreload, true);
                    pathState = 1;
                }
                break;

            case 1: // Drive to score preload
                if (!follower.isBusy()) {
                    scoringAction.startScoring();
                    pathState = 2;
                }
                break;

            case 2: // Wait for scoring to complete
                if (!scoringAction.isScoring()) {
                    waitTimer.reset();
                    pathState = 3;
                }
                break;

            case 3: // Wait after scoring
                if (waitTimer.seconds() >= POST_SCORE_WAIT) {
                    intake.startIntake();
                    follower.followPath(paths.GrabPickup1ToIntake, true);
                    pathState = 4;
                }
                break;

            case 4: // Drive to intake position
                if (!follower.isBusy()) {
                    follower.followPath(paths.GrabPickup1ToPickup, true);
                    pathState = 5;
                }
                break;

            case 5: // Drive to pickup 1
                if (!follower.isBusy()) {
                    follower.followPath(paths.GrabPickup1Setup, true);
                    pathState = 6;
                }
                break;

            case 6: // Drive to gate setup position
                if (!follower.isBusy()) {
                    follower.followPath(paths.GrabPickup1Empty, true);
                    pathState = 7;
                }
                break;

            case 7: // Open gate
                if (!follower.isBusy()) {
                    follower.followPath(paths.ScorePickup1, true);
                    pathState = 8;
                }
                break;

            case 8: // Drive back to score pickup 1
                if (!follower.isBusy()) {
                    intake.stop();
                    scoringAction.startScoring();
                    pathState = 9;
                }
                break;

            case 9: // Wait for scoring to complete
                if (!scoringAction.isScoring()) {
                    waitTimer.reset();
                    pathState = 10;
                }
                break;

            case 10: // Wait after scoring
                if (waitTimer.seconds() >= POST_SCORE_WAIT) {
                    intake.startIntake();
                    follower.followPath(paths.GrabPickup2ToIntake, true);
                    pathState = 11;
                }
                break;

            case 11: // Drive to intake position for pickup 2
                if (!follower.isBusy()) {
                    follower.followPath(paths.GrabPickup2ToPickup, true);
                    pathState = 12;
                }
                break;

            case 12: // Drive to pickup 2
                if (!follower.isBusy()) {
                    follower.followPath(paths.ScorePickup2, true);
                    pathState = 13;
                }
                break;

            case 13: // Drive back to score pickup 2
                if (!follower.isBusy()) {
                    intake.stop();
                    scoringAction.startScoring();
                    pathState = 14;
                }
                break;

            case 14: // Wait for scoring to complete
                if (!scoringAction.isScoring()) {
                    waitTimer.reset();
                    pathState = 15;
                }
                break;

            case 15: // Wait after scoring
                if (waitTimer.seconds() >= POST_SCORE_WAIT) {
                    intake.startIntake();
                    follower.followPath(paths.GrabPickup3ToIntake, true);
                    pathState = 16;
                }
                break;

            case 16: // Drive to intake position for pickup 3
                if (!follower.isBusy()) {
                    follower.followPath(paths.GrabPickup3ToPickup, true);
                    pathState = 17;
                }
                break;

            case 17: // Drive to pickup 3
                if (!follower.isBusy()) {
                    follower.followPath(paths.ScorePickup3, true);
                    pathState = 18;
                }
                break;

            case 18: // Drive back to score pickup 3
                if (!follower.isBusy()) {
                    intake.stop();
                    scoringAction.startScoring();
                    pathState = 19;
                }
                break;

            case 19: // Wait for scoring to complete
                if (!scoringAction.isScoring()) {
                    waitTimer.reset();
                    pathState = 20;
                }
                break;

            case 20: // Wait after final scoring
                if (waitTimer.seconds() >= POST_SCORE_WAIT) {
                    shooter.turnOff();
                    follower.followPath(paths.Leave, true);
                    pathState = 21;
                }
                break;

            case 21: // Drive to leave
                if (!follower.isBusy()) {
                    autoEndPose = follower.getPose();
                    pathState = 22;
                }
                break;

            case 22: // Complete
                break;
        }

        return pathState;
    }

    @Override
    public void stop() {
        scoringAction.stopScoring();
        shooter.turnOff();
        turret.stop();
        autoEndPose = follower.getPose();
    }

    public static class Paths {
        public PathChain ScorePreload;
        public PathChain GrabPickup1ToIntake;
        public PathChain GrabPickup1ToPickup;
        public PathChain GrabPickup1Setup;
        public PathChain GrabPickup1Empty;
        public PathChain ScorePickup1;
        public PathChain GrabPickup2ToIntake;
        public PathChain GrabPickup2ToPickup;
        public PathChain ScorePickup2;
        public PathChain GrabPickup3ToIntake;
        public PathChain GrabPickup3ToPickup;
        public PathChain ScorePickup3;
        public PathChain Leave;

        public Paths(Follower follower) {
            ScorePreload = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(21.000, 122.000),
                                    new Pose(60.000, 83.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(139.5), Math.toRadians(180))
                    .build();

            GrabPickup1ToIntake = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 83.500),
                                    new Pose(52.000, 83.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup1ToPickup = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(52.000, 83.500),
                                    new Pose(14.408, 83.703)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup1Setup = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(14.408, 83.703),
                                    new Pose(30.000, 77.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90))
                    .build();

            GrabPickup1Empty = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(30.000, 77.500),
                                    new Pose(16.000, 76.000)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(90))
                    .build();

            ScorePickup1 = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(16.000, 76.000),
                                    new Pose(60.000, 83.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup2ToIntake = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 83.500),
                                    new Pose(60.000, 59.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup2ToPickup = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 59.500),
                                    new Pose(8.000, 59.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            ScorePickup2 = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(8.000, 59.500),
                                    new Pose(60.000, 83.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup3ToIntake = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 83.500),
                                    new Pose(60.000, 34.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            GrabPickup3ToPickup = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 34.500),
                                    new Pose(10.000, 34.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            ScorePickup3 = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(10.000, 34.500),
                                    new Pose(60.000, 83.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            Leave = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(60.000, 83.500),
                                    new Pose(50.000, 73.500)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(135))
                    .build();
        }
    }
}