package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Shooter {
    private DcMotorEx shooterUp;
    private DcMotorEx shooterDown;

    public enum ShotMode {
        OFF,
        NEAR,    // Close shot
        FAR      // Far shot
    }

    private ShotMode currentShotMode = ShotMode.OFF;

    private static final double NEAR_VELOCITY = 1170; //needs tuning
    private static final double FAR_VELOCITY = 1400; //needs tuning
    private static final double OFF_VELOCITY = 0.0;

    private boolean lastLeftBumper = false;
    private boolean lastRightBumper = false;
    private boolean lastBButton = false;

    private static final double P = 32;
    private static final double I = 0;
    private static final double D = 0;
    private static final double F = 20;

    private double lastTargetVelocity = 0;

    public Shooter(HardwareMap hardwareMap) {
        shooterUp = hardwareMap.get(DcMotorEx.class, "shooterUp");
        shooterDown = hardwareMap.get(DcMotorEx.class, "shooterDown");

        shooterUp.setVelocityPIDFCoefficients(P, I, D, F);
        shooterDown.setVelocityPIDFCoefficients(P, I, D, F);

        shooterUp.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooterDown.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        shooterUp.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterDown.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void updateShotMode(Gamepad gamepad) {
        if (gamepad.left_bumper && !lastLeftBumper) {
            currentShotMode = ShotMode.NEAR;
        }
        if (gamepad.right_bumper && !lastRightBumper) {
            currentShotMode = ShotMode.FAR;
        }
        if (gamepad.b && !lastBButton) {
            currentShotMode = ShotMode.OFF;
        }

        lastLeftBumper = gamepad.left_bumper;
        lastRightBumper = gamepad.right_bumper;
        lastBButton = gamepad.b;

        updateVelocity();
    }

    public void updateVelocity() {
        double targetVelocity = getPresetVelocity();
        lastTargetVelocity = targetVelocity;
        setVelocity(targetVelocity);
    }

    private double getPresetVelocity() {
        switch (currentShotMode) {
            case NEAR:
                return NEAR_VELOCITY;
            case FAR:
                return FAR_VELOCITY;
            case OFF:
            default:
                return OFF_VELOCITY;
        }
    }

    public void setNearShot() {
        currentShotMode = ShotMode.NEAR;
        updateVelocity();
    }

    public void setFarShot() {
        currentShotMode = ShotMode.FAR;
        updateVelocity();
    }

    public void turnOff() {
        currentShotMode = ShotMode.OFF;
        updateVelocity();
    }

    public void setVelocity(double velocity) {
        shooterUp.setVelocity(velocity);
        shooterDown.setVelocity(velocity);
    }

    public double getCurrentVelocity() {
        return (shooterUp.getVelocity() + shooterDown.getVelocity()) / 2.0;
    }

    public double getUpVelocity() {
        return shooterUp.getVelocity();
    }

    public double getDownVelocity() {
        return shooterDown.getVelocity();
    }

    public ShotMode getCurrentShotMode() {
        return currentShotMode;
    }
}