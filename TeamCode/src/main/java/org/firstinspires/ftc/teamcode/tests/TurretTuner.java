package org.firstinspires.ftc.teamcode.tests;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Turret;

@Configurable
@TeleOp(name = "Turret Tuner", group = "Tuning")
public class TurretTuner extends OpMode {

    private Turret turret;
    private TelemetryManager telemetryM;

    public static boolean useManualPower = true;
    public static double manualPower = 0.0;      // Manual power (-1.0 to 1.0)
    public static double targetAngle = 0.0;      // Target angle for PID mode (degrees)

    @Override
    public void init() {
        turret = new Turret(hardwareMap);
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        telemetry.addLine("Turret Tuner Ready!");
        telemetry.addLine();
        telemetry.addLine("STEP 1: Test direction with manual power");
        telemetry.addLine("- Set useManualPower = true");
        telemetry.addLine("- Adjust manualPower in PANELS");
        telemetry.addLine("- Positive should rotate clockwise (from top view)");
        telemetry.addLine();
        telemetry.addLine("STEP 2: Zero the turret");
        telemetry.addLine("- Point turret forward");
        telemetry.addLine("- Press START to reset encoder");
        telemetry.addLine();
        telemetry.addLine("STEP 3: Tune PID");
        telemetry.addLine("- Set useManualPower = false");
        telemetry.addLine("- Set targetAngle in PANELS");
        telemetry.addLine("- Adjust kP, kI, kD until smooth");
        telemetry.update();
    }

    @Override
    public void start() {
        turret.resetEncoder();
    }

    @Override
    public void loop() {
        if (gamepad1.start) {
            turret.resetEncoder();
        }

        if (useManualPower) {
            turret.setPower(manualPower);
        } else {
            turret.setTargetAngle(targetAngle);
            turret.update();
        }

        telemetryM.debug("=== MODE ===", "");
        telemetryM.debug("Manual Power Mode", useManualPower);
        telemetryM.debug("Manual Power", manualPower);
        telemetryM.debug("Target Angle", targetAngle);
        telemetryM.debug("", "");

        telemetryM.debug("=== STATUS ===", "");
        telemetryM.debug("Current Angle", "%.2f°", turret.getCurrentAngle());
        telemetryM.debug("Angle Error", "%.2f°", turret.getAngleError());
        telemetryM.debug("At Target", turret.atTarget());
        telemetryM.debug("", "");

        telemetryM.debug("=== PID ===", "");
        telemetryM.debug("kP", Turret.kP);
        telemetryM.debug("kI", Turret.kI);
        telemetryM.debug("kD", Turret.kD);

        telemetryM.update();

        telemetry.addLine("=== MODE ===");
        telemetry.addData("Manual Power Mode", useManualPower);
        telemetry.addData("Manual Power", "%.2f", manualPower);
        telemetry.addData("Target Angle", "%.2f°", targetAngle);
        telemetry.addLine();

        telemetry.addLine("=== STATUS ===");
        telemetry.addData("Current Angle", "%.2f°", turret.getCurrentAngle());
        telemetry.addData("Angle Error", "%.2f°", turret.getAngleError());
        telemetry.addData("At Target", turret.atTarget());
        telemetry.addLine();

        telemetry.addLine("=== PID ===");
        telemetry.addData("kP", Turret.kP);
        telemetry.addData("kI", Turret.kI);
        telemetry.addData("kD", Turret.kD);
        telemetry.addLine();

        telemetry.addLine("Press START to reset encoder");
        telemetry.update();
    }

    @Override
    public void stop() {
        turret.stop();
    }
}