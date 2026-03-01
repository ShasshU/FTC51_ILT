package org.firstinspires.ftc.teamcode.subsystems;
import android.graphics.Color;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

public class ColorSensorsAndLight {
    private NormalizedColorSensor colorSensor1;
    private Servo Light = null;
    private float[] hsvValues = new float[3];

    public enum DetectedColor {
        GREEN,
        PURPLE,
        NONE
    }

    public ColorSensorsAndLight(HardwareMap hardwareMap) {
        colorSensor1 = hardwareMap.get(NormalizedColorSensor.class, "Color1");
        Light = hardwareMap.get(Servo.class, "Light");
    }

    public DetectedColor getColor() {

        NormalizedRGBA colors = colorSensor1.getNormalizedColors();

        float r = colors.red;
        float g = colors.green;
        float b = colors.blue;

        Color.RGBToHSV(
                (int)(r * 255),
                (int)(g * 255),
                (int)(b * 255),
                hsvValues
        );

        float hue = hsvValues[0];
        float saturation = hsvValues[1];

        if (hue > 110 && hue < 190 && saturation > 0.4) {
            Light.setPosition(0.5);
            return DetectedColor.GREEN;
        }
        else if (hue > 200 && hue < 250 && saturation > 0.4) {
            Light.setPosition(0.5);
            return DetectedColor.PURPLE;
        }
        else {
            Light.setPosition(0.388);
            return DetectedColor.NONE;
        }
    }
    public void setPosition(double position) {
        Light.setPosition(position);
    }
    public float getHue() {
        return hsvValues[0];
    }
}