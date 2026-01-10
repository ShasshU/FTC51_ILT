package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.util.ElapsedTime;

public class ScoringAction {

    private Intake intake;
    private Kicker kicker;
    private ElapsedTime timer;

    // Timing constants
    private static final double INTAKE_TIME = 0.8; // Run intake for 0.8 seconds
    private static final double KICK_COMPLETE_WAIT = 0.3; // Wait for pulse to complete (PULSE_EXTEND_TIME + buffer)

    // State machine
    private ScoringState currentState = ScoringState.IDLE;

    public enum ScoringState {
        IDLE,
        INTAKING,      // Running intake
        KICKING,       // Kicker pulsing
        WAITING_KICK,  // Wait for kick pulse to complete
        COMPLETE
    }

    public ScoringAction(Intake intake, Kicker kicker) {
        this.intake = intake;
        this.kicker = kicker;
        this.timer = new ElapsedTime();
    }

    // Start the scoring sequence
    public void startScoring() {
        if (currentState == ScoringState.IDLE) {
            currentState = ScoringState.INTAKING;
            intake.startIntake();
            timer.reset();
        }
    }

    // Stop scoring sequence (emergency stop)
    public void stopScoring() {
        intake.stop();
        kicker.retract();
        currentState = ScoringState.IDLE;
    }

    // Call this every loop to update the state machine
    public void update() {
        // CRITICAL: Update kicker for pulse timing
        kicker.update();

        switch (currentState) {
            case IDLE:
                // Do nothing, waiting for startScoring()
                break;

            case INTAKING:
                // Wait for intake time, then kick
                if (timer.seconds() >= INTAKE_TIME) {
                    kicker.pulse(); // Use pulse instead of extend!
                    currentState = ScoringState.KICKING;
                    timer.reset();
                }
                break;

            case KICKING:
                // Wait for pulse to complete (kicker will auto-retract via update())
                if (timer.seconds() >= KICK_COMPLETE_WAIT) {
                    intake.stop();
                    currentState = ScoringState.COMPLETE;
                }
                break;

            case COMPLETE:
                // Scoring done, return to idle
                currentState = ScoringState.IDLE;
                break;
        }
    }

    // Status checks
    public boolean isScoring() {
        return currentState != ScoringState.IDLE && currentState != ScoringState.COMPLETE;
    }

    public ScoringState getCurrentState() {
        return currentState;
    }

    public double getStateTime() {
        return timer.seconds();
    }
}