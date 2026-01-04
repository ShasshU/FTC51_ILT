package org.firstinspires.ftc.teamcode.autos;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Configurable
@Autonomous(name = "RedClose12Piece", group = "Autonomous")
public class RedClose12Piece extends OpMode {

    public Follower follower;
    private Turret turret;
    private int pathState;
    private Paths paths;
    private ElapsedTime waitTimer;

    private static final Pose startPose = new Pose(124, 123, Math.toRadians(40.5));

    private static final double PATH_WAIT = 0.4;

    public static Pose autoEndPose = null;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startPose);

        turret = new Turret(hardwareMap);
        turret.resetEncoder();

        waitTimer = new ElapsedTime();

        paths = new Paths(follower);

        telemetry.addLine("Test Auto Initialized!");
        telemetry.update();
    }

    @Override
    public void start() {
        pathState = 0;
    }

    @Override
    public void loop() {
        follower.update();

        turret.aimAtGoal(
                follower.getPose().getX(),
                follower.getPose().getY(),
                follower.getPose().getHeading(),
                false  // false = RED alliance
        );
        turret.update();

        pathState = autonomousPathUpdate();

        telemetry.addData("Path State", pathState);
        telemetry.addData("Turret Angle", "%.1f°", turret.getCurrentAngle());
        telemetry.addData("Turret Target", "%.1f°", turret.getTargetAngle());
        telemetry.addData("At Target", turret.atTarget());
        telemetry.addData("X", follower.getPose().getX());
        telemetry.addData("Y", follower.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
        telemetry.update();
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 0: // Drive to first position
                follower.followPath(paths.Path1, true);
                pathState = 1;
                break;

            case 1: // Wait for path to complete
                if (!follower.isBusy()) {
                    waitTimer.reset();
                    pathState = 2;
                }
                break;

            case 2: // Wait
                if (waitTimer.seconds() >= PATH_WAIT) {
                    follower.followPath(paths.Path2, true);
                    pathState = 3;
                }
                break;

            case 3: // Continue to next position
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path3, true);
                    pathState = 4;
                }
                break;

            case 4: // Drive back
                if (!follower.isBusy()) {
                    waitTimer.reset();
                    pathState = 5;
                }
                break;

            case 5: // Wait
                if (waitTimer.seconds() >= PATH_WAIT) {
                    follower.followPath(paths.Path4, true);
                    pathState = 6;
                }
                break;

            case 6: // Continue
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path5, true);
                    pathState = 7;
                }
                break;

            case 7: // Drive back
                if (!follower.isBusy()) {
                    waitTimer.reset();
                    pathState = 8;
                }
                break;

            case 8: // Wait
                if (waitTimer.seconds() >= PATH_WAIT) {
                    follower.followPath(paths.Path6, true);
                    pathState = 9;
                }
                break;

            case 9: // Continue
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path7, true);
                    pathState = 10;
                }
                break;

            case 10: // Drive back
                if (!follower.isBusy()) {
                    waitTimer.reset();
                    pathState = 11;
                }
                break;

            case 11: // Wait
                if (waitTimer.seconds() >= PATH_WAIT) {
                    follower.followPath(paths.Leave, true);
                    pathState = 12;
                }
                break;

            case 12: // Leave
                if (!follower.isBusy()) {
                    autoEndPose = follower.getPose();
                    pathState = 13;
                }
                break;

            case 13: // Complete
                break;
        }

        return pathState;
    }

    @Override
    public void stop() {
        turret.stop();
        autoEndPose = follower.getPose();
    }

    public static class Paths {
        public PathChain Path1;
        public PathChain Path2;
        public PathChain Path3;
        public PathChain Path4;
        public PathChain Path5;
        public PathChain Path6;
        public PathChain Path7;
        public PathChain Leave;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(124, 123), new Pose(84.085, 83.882)))
                    .setLinearHeadingInterpolation(Math.toRadians(40.5), Math.toRadians(42.5))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(84.085, 83.882), new Pose(95, 82.678)))
                    .setLinearHeadingInterpolation(Math.toRadians(42.5), Math.toRadians(0))
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(95, 82.678), new Pose(131.189, 82.882)))
                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                    .build();

            Path4 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(131.189, 82.882), new Pose(84.1, 83.882)))
                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(40.5))
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(84.1, 83.882), new Pose(84, 57.5)))
                    .setLinearHeadingInterpolation(Math.toRadians(40.5), Math.toRadians(0))
                    .build();

            Path6 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(84, 57.5), new Pose(134.5, 57.5)))
                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                    .build();

            Path7 = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(134.5, 57.5), new Pose(84.085, 83.882)))
                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(42.5))
                    .build();

            Leave = follower
                    .pathBuilder()
                    .addPath(new BezierLine(new Pose(84.085, 83.882), new Pose(94, 73.5)))
                    .setLinearHeadingInterpolation(Math.toRadians(42.5), Math.toRadians(42.5))
                    .build();
        }
    }
}