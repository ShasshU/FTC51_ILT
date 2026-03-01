package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.ColorSensorsAndLight;

@Configurable
@TeleOp(name = "colorTest", group = "Teleop")
public class colorSensorTest extends OpMode {
   private ColorSensorsAndLight colorSensor1;
   private ColorSensorsAndLight Light;
   private float[] hsvValues = new float[3];
//   private ColorSensors colorSensor2;
//   private ColorSensors colorSensor3;







    @Override
    public void init() {
        colorSensor1 = new ColorSensorsAndLight(hardwareMap);
        Light = new ColorSensorsAndLight(hardwareMap);
        Light.setPosition(0.388);
//        colorSensor2 = new ColorSensors(hardwareMap);
//        colorSensor3 = new ColorSensors(hardwareMap);
    }

    @Override
    public void loop() {
        ColorSensorsAndLight.DetectedColor color = colorSensor1.getColor();

        telemetry.addData("Detected", color);
        telemetry.addData("Hue", colorSensor1.getHue());
        telemetry.update();
    }
}
