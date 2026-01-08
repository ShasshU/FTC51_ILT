package org.firstinspires.ftc.teamcode.tests;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
@TeleOp(name = "Kicker Position Tuner", group = "Tuning")
public class KickerTuner extends OpMode {

    private Servo kicker;
    private TelemetryManager telemetryM;

    // Servo positions (tunable in PANELS)
    public static double extendedPosition = 0.5;  // Start at middle
    public static double retractedPosition = 0.0; // Start at 0

    // Control
    public static boolean useExtended = false; // false = retracted, true = extended

    @Override
    public void init() {
        kicker = hardwareMap.get(Servo.class, "kicker");
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        // Start retracted
        kicker.setPosition(retractedPosition);

        telemetry.addLine("Kicker Position Tuner Ready!");
        telemetry.addLine();
        telemetry.addLine("STEP 1: Find RETRACTED position");
        telemetry.addLine("- Toggle useExtended = false");
        telemetry.addLine("- Adjust retractedPosition in PANELS");
        telemetry.addLine("- Servo should be fully retracted (not touching ball)");
        telemetry.addLine();
        telemetry.addLine("STEP 2: Find EXTENDED position");
        telemetry.addLine("- Toggle useExtended = true");
        telemetry.addLine("- Adjust extendedPosition in PANELS");
        telemetry.addLine("- Servo should push ball into shooter");
        telemetry.addLine();
        telemetry.addLine("STEP 3: Test switching");
        telemetry.addLine("- Toggle useExtended back and forth");
        telemetry.addLine("- Make sure kick motion is smooth");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Set servo position based on mode
        if (useExtended) {
            kicker.setPosition(extendedPosition);
        } else {
            kicker.setPosition(retractedPosition);
        }

        if (gamepad1.dpad_up) {
            kicker.setPosition(1.0);
        }
        if (gamepad1.dpad_down) {
            kicker.setPosition(0.0);
        }
        if (gamepad1.dpad_left) {
            kicker.setPosition(0.5);
        }

        // Send to PANELS
        telemetryM.debug("=== MODE ===", "");
        telemetryM.debug("Extended", useExtended);
        telemetryM.debug("", "");

        telemetryM.debug("=== POSITIONS ===", "");
        telemetryM.debug("Retracted Position", retractedPosition);
        telemetryM.debug("Extended Position", extendedPosition);
        telemetryM.debug("Current Position", kicker.getPosition());

        telemetryM.update();

        // Also to Driver Station
        telemetry.addLine("=== MODE ===");
        telemetry.addData("Extended", useExtended);
        telemetry.addLine();

        telemetry.addLine("=== POSITIONS ===");
        telemetry.addData("Retracted", "%.3f", retractedPosition);
        telemetry.addData("Extended", "%.3f", extendedPosition);
        telemetry.addData("Current", "%.3f", kicker.getPosition());
        telemetry.addLine();

        telemetry.addLine("Toggle 'useExtended' in PANELS to switch");
        telemetry.update();
    }

    @Override
    public void stop() {
        // Return to retracted on stop
        kicker.setPosition(retractedPosition);
    }
}