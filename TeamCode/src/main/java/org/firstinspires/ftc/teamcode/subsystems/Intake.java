package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

public class Intake {

    public enum IntakeState {
        OFF,
        INTAKE,
        OUTTAKE
    }

    private DcMotorEx intake;
    private CRServo intakeServo;
    private IntakeState currentState = IntakeState.OFF;

    private static final double INTAKE_POWER = 1.0;
    private static final double OUTTAKE_POWER = -1.0;
    private static final double OFF_POWER = 0.0;

    private static final double SERVO_INTAKE_POWER = 1.0;
    private static final double SERVO_OUTTAKE_POWER = -1.0;
    private static final double SERVO_OFF_POWER = 0.0;

    public Intake(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        intakeServo = hardwareMap.get(CRServo.class, "kicker");

        intake.setDirection(DcMotorSimple.Direction.FORWARD);
        intakeServo.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void toggleIntake() {
        if (currentState == IntakeState.INTAKE) {
            stop();
        } else {
            startIntake();
        }
    }

    public void toggleOuttake() {
        if (currentState == IntakeState.OUTTAKE) {
            stop();
        } else {
            startOuttake();
        }
    }

    public void startIntake() {
        currentState = IntakeState.INTAKE;
        intake.setPower(INTAKE_POWER);
        intakeServo.setPower(SERVO_INTAKE_POWER);
    }

    public void startOuttake() {
        currentState = IntakeState.OUTTAKE;
        intake.setPower(OUTTAKE_POWER);
        intakeServo.setPower(SERVO_OUTTAKE_POWER);
    }

    public void stop() {
        currentState = IntakeState.OFF;
        intake.setPower(OFF_POWER);
        intakeServo.setPower(SERVO_OFF_POWER);
    }

    public void setPower(double power) {
        intake.setPower(power);
        intakeServo.setPower(power);
    }

    public IntakeState getState() {
        return currentState;
    }
}