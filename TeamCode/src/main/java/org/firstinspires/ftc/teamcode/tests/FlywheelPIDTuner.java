package org.firstinspires.ftc.teamcode.tests;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Configurable
@TeleOp(name = "Flywheel PID Tuner", group = "Tuning")
public class FlywheelPIDTuner extends OpMode {

    private DcMotorEx shooterUp;
    private DcMotorEx shooterDown;
    private TelemetryManager telemetryM;

    public static double P = 32;
    public static double I = 0;
    public static double D = 0;
    public static double F = 20;

    public static double targetVelocity = 1200;

    @Override
    public void init() {
        shooterUp = hardwareMap.get(DcMotorEx.class, "shooterUp");
        shooterDown = hardwareMap.get(DcMotorEx.class, "shooterDown");

        shooterUp.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterDown.setDirection(DcMotorSimple.Direction.FORWARD);

        shooterUp.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooterDown.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        telemetry.addLine("Flywheel PID Tuner Ready!");
        telemetry.addLine("Use PANELS to adjust P, I, D, F, and targetVelocity");
        telemetry.update();
    }

    @Override
    public void loop() {
        shooterUp.setVelocityPIDFCoefficients(P, I, D, F);
        shooterDown.setVelocityPIDFCoefficients(P, I, D, F);

        shooterUp.setVelocity(targetVelocity);
        shooterDown.setVelocity(targetVelocity);

        double upVelocity = shooterUp.getVelocity();
        double downVelocity = shooterDown.getVelocity();
        double avgVelocity = (upVelocity + downVelocity) / 2.0;

        double upError = targetVelocity - upVelocity;
        double downError = targetVelocity - downVelocity;
        double avgError = targetVelocity - avgVelocity;

        telemetryM.debug("Target Velocity", targetVelocity);
        telemetryM.debug("Up Velocity", upVelocity);
        telemetryM.debug("Down Velocity", downVelocity);
        telemetryM.debug("Avg Velocity", avgVelocity);
        telemetryM.debug("Up Error", upError);
        telemetryM.debug("Down Error", downError);
        telemetryM.debug("Avg Error", avgError);
        telemetryM.debug("P", P);
        telemetryM.debug("I", I);
        telemetryM.debug("D", D);
        telemetryM.debug("F", F);
        telemetryM.update();

        telemetry.addData("Target", "%.0f", targetVelocity);
        telemetry.addData("Up", "%.0f (err: %.0f)", upVelocity, upError);
        telemetry.addData("Down", "%.0f (err: %.0f)", downVelocity, downError);
        telemetry.addData("Avg", "%.0f (err: %.0f)", avgVelocity, avgError);
        telemetry.addLine();
        telemetry.addData("P", P);
        telemetry.addData("I", I);
        telemetry.addData("D", D);
        telemetry.addData("F", F);
        telemetry.update();
    }

    @Override
    public void stop() {
        shooterUp.setVelocity(0);
        shooterDown.setVelocity(0);
    }
}