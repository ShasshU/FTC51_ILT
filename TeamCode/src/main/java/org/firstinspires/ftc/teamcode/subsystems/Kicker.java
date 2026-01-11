package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Configurable
public class Kicker {

    private Servo kicker;
    private ElapsedTime timer;

    // Servo positions (adjust these after tuning)
    public static double EXTENDED_POSITION = 0.6;   // TODO: Tune this value
    public static double RETRACTED_POSITION = 0.91;  // TODO: Tune this value

    // Pulse timing
    private static final double PULSE_EXTEND_TIME = 0.2;  // How long to stay extended

    // State tracking
    private boolean isPulsing = false;
    private KickerState currentState = KickerState.RETRACTED;

    public enum KickerState {
        RETRACTED,
        EXTENDED,
        PULSING
    }

    public Kicker(HardwareMap hardwareMap) {
        kicker = hardwareMap.get(Servo.class, "kicker");
        timer = new ElapsedTime();
        retract(); // Start retracted
    }

    // Manual control
    public void extend() {
        kicker.setPosition(EXTENDED_POSITION);
        currentState = KickerState.EXTENDED;
        isPulsing = false;
    }

    public void retract() {
        kicker.setPosition(RETRACTED_POSITION);
        currentState = KickerState.RETRACTED;
        isPulsing = false;
    }

    // Automatic pulse (extend → wait → retract)
    public void pulse() {
        extend();
        timer.reset();
        isPulsing = true;
        currentState = KickerState.PULSING;
    }

    // Call this every loop to handle automatic pulse retraction
    public void update() {
        if (isPulsing && timer.seconds() >= PULSE_EXTEND_TIME) {
            retract();
        }
    }

    // Status
    public boolean isPulsing() {
        return isPulsing;
    }

    public KickerState getCurrentState() {
        return currentState;
    }

    public double getCurrentPosition() {
        return kicker.getPosition();
    }
}