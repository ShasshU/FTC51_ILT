package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Configurable
public class Turret {

    private DcMotorEx turretMotor;

    private static final double MOTOR_CPR = 537.7; // goBILDA 5203-1150 RPM
    private static final double GEAR_RATIO = 3.18; // 3.18:1 reduction (motor spins 3.18x per turret rotation)
    private static final double TICKS_PER_TURRET_ROTATION = MOTOR_CPR * (3.18/ 2.9);
    public static double TURRET_OFFSET_X = -6.65; // 6.65 inches behind center
    public static double TURRET_OFFSET_Y = 0;     // Centered left-to-right

    private static final double MAX_ANGLE = 90;  // previously 180 from 1/3
    private static final double MIN_ANGLE = -90; // previously -180 from 1/3


    public static double kP = 0.0095;
    public static double kI = 0.001;
    public static double kD = 0.001;

    public static double POSITION_TOLERANCE = 2.0;


    private double targetAngleDegrees = 0;
    private int targetPositionTicks = 0;


    public Turret(HardwareMap hardwareMap) {
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");

        turretMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public void resetEncoder() {
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        targetAngleDegrees = 0;
        targetPositionTicks = 0;
    }


    public void aimAtGoal(double robotX, double robotY, double robotHeading, boolean isBlue) {
        double turretX = robotX + TURRET_OFFSET_X;
        double turretY = robotY + TURRET_OFFSET_Y;

        double goalX = isBlue ? 0 : 144;
        double goalY = 144;

        double angleToGoalRadians = Math.atan2(goalX - turretX, goalY - turretY);
        double angleToGoalDegrees = Math.toDegrees(angleToGoalRadians);

        double robotHeadingDegrees = Math.toDegrees(robotHeading);
        double turretAngle = angleToGoalDegrees + robotHeadingDegrees - 90;

        turretAngle = normalizeAngle(turretAngle);

        setTargetAngle(turretAngle);
    }


    public void setTargetAngle(double angleDegrees) {
        angleDegrees = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, angleDegrees));

        targetAngleDegrees = angleDegrees;
        targetPositionTicks = degreesToTicks(angleDegrees);
    }

    public void setPower(double power) {
        turretMotor.setPower(power);
    }

    public void stop() {
        turretMotor.setPower(0);
    }


    public void update() {
        int currentTicks = turretMotor.getCurrentPosition();

        int error = targetPositionTicks - currentTicks;

        double power = kP * error;

        power = Math.max(-1.0, Math.min(1.0, power));

        turretMotor.setPower(power);
    }

    public boolean atTarget() {
        int currentTicks = turretMotor.getCurrentPosition();
        return Math.abs(targetPositionTicks - currentTicks) < POSITION_TOLERANCE;
    }

    public double getCurrentAngle() {
        return ticksToDegrees(turretMotor.getCurrentPosition());
    }

    public double getTargetAngle() {
        return targetAngleDegrees;
    }


    public double getAngleError() {
        return targetAngleDegrees - getCurrentAngle();
    }

    private int degreesToTicks(double degrees) {
        return (int) ((degrees / 360.0) * TICKS_PER_TURRET_ROTATION);
    }

    private double ticksToDegrees(int ticks) {
        return (ticks / TICKS_PER_TURRET_ROTATION) * 360.0;
    }

    private double normalizeAngle(double angleDegrees) {
        while (angleDegrees > 180) angleDegrees -= 360;
        while (angleDegrees < -180) angleDegrees += 360;
        return angleDegrees;
    }
}