package org.firstinspires.ftc.teamcode.tests;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Configurable
@TeleOp(name = "Shooter Direction Test", group = "Tuning")
public class ShooterTest extends OpMode {

    private DcMotorEx shooterUp;
    private DcMotorEx shooterDown;

    public static double testPower = 0.3; // Low power for safe testing

    @Override
    public void init() {
        shooterUp = hardwareMap.get(DcMotorEx.class, "shooterUp");
        shooterDown = hardwareMap.get(DcMotorEx.class, "shooterDown");

        // Both FORWARD to start
        shooterUp.setDirection(DcMotorSimple.Direction.FORWARD);
        shooterDown.setDirection(DcMotorSimple.Direction.FORWARD);

        shooterUp.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooterDown.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Shooter Direction Test");
        telemetry.addLine("Press A = Test Up motor");
        telemetry.addLine("Press B = Test Down motor");
        telemetry.addLine("Press X = Test BOTH motors");
        telemetry.addLine();
        telemetry.addLine("Watch which way each flywheel spins!");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Test individual motors
        if (gamepad1.a) {
            shooterUp.setPower(testPower);
            shooterDown.setPower(0);
            telemetry.addLine(">>> Testing UP motor <<<");
        } else if (gamepad1.b) {
            shooterUp.setPower(0);
            shooterDown.setPower(testPower);
            telemetry.addLine(">>> Testing DOWN motor <<<");
        } else if (gamepad1.x) {
            shooterUp.setPower(testPower);
            shooterDown.setPower(testPower);
            telemetry.addLine(">>> Testing BOTH motors <<<");
        } else {
            shooterUp.setPower(0);
            shooterDown.setPower(0);
            telemetry.addLine("Press A/B/X to test");
        }

        telemetry.addData("Up Velocity", shooterUp.getVelocity());
        telemetry.addData("Down Velocity", shooterDown.getVelocity());
        telemetry.update();
    }

    @Override
    public void stop() {
        shooterUp.setPower(0);
        shooterDown.setPower(0);
    }
}