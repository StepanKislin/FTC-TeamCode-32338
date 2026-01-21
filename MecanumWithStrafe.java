package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

@TeleOp(name = "Mecanum + Обычный Серво", group = "Drive")
public class MecanumWithStrafe extends LinearOpMode {

    // Для ОБЫЧНОГО серво (часто):
    // 0.0 = ЗАКРЫТО (захват сжат)
    // 1.0 = ОТКРЫТО (захват разжат)
    private static final double SERVO_OPEN   = 1.0;  // ← ОТКРЫТО для обычного серво
    private static final double SERVO_CLOSED = 0.0; // ← ЗАКРЫТО для обычного серво

    private static final double TANK_TURN_THRESHOLD = 0.9;
    private static final double TURN_POWER = 0.7;
    private static final double POWER_STEP = 0.05;

    @Override
    public void runOpMode() {
        // === ШАССИ ===
        DcMotor leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftRear   = hardwareMap.get(DcMotor.class, "leftRear");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightRear  = hardwareMap.get(DcMotor.class, "rightRear");

        // === ВЫСТРЕЛ ===
        DcMotor extraMotor = hardwareMap.get(DcMotor.class, "extraMotor");
        DcMotor leftExtra  = hardwareMap.get(DcMotor.class, "leftExtra");
        DcMotor rightExtra = hardwareMap.get(DcMotor.class, "rightExtra");

        // === РУКА ===
        DcMotor catchMotor = hardwareMap.get(DcMotor.class, "catchMotor");

        // === ОБЫЧНЫЙ СЕРВО (порт 0) ===
        Servo servo = hardwareMap.get(Servo.class, "servo");

        // Направления колёс
        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftRear.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightRear.setDirection(DcMotor.Direction.FORWARD);

        // Все моторы без энкодеров
        extraMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftExtra.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightExtra.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        catchMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        VoltageSensor batterySensor = hardwareMap.voltageSensor.iterator().next();

        double powerLevel = 0.75;
        double currentServoPosition = SERVO_OPEN; // Начинаем с ОТКРЫТОГО

        // Устанавливаем начальную позицию
        servo.setPosition(currentServoPosition);

        telemetry.addLine("Обычный серво: X=закрыть (0.0), Y=открыть (1.0)");
        telemetry.addLine("Если не так — поменяй SERVO_OPEN/CLOSED в коде!");
        telemetry.update();
        waitForStart();

        servo.setPosition(currentServoPosition); // Фикс после старта

        while (opModeIsActive()) {
            // === УПРАВЛЕНИЕ СЕРВО ===
            if (gamepad1.x) {
                currentServoPosition = SERVO_CLOSED; // 0.0 = закрыто
            } else if (gamepad1.y) {
                currentServoPosition = SERVO_OPEN;   // 1.0 = открыто
            }
            servo.setPosition(currentServoPosition); // Обязательно каждый цикл!

            // === УПРАВЛЕНИЕ ШАССИ ===
            double drive  = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn   = -gamepad1.right_stick_x;

            boolean tankTurnRight = Math.abs(gamepad1.right_stick_x) > TANK_TURN_THRESHOLD;
            if (tankTurnRight) {
                leftFront.setPower(TURN_POWER * Math.signum(gamepad1.right_stick_x));
                leftRear.setPower(TURN_POWER * Math.signum(gamepad1.right_stick_x));
                rightFront.setPower(-TURN_POWER * Math.signum(gamepad1.right_stick_x));
                rightRear.setPower(-TURN_POWER * Math.signum(gamepad1.right_stick_x));
            } else {
                double lf = drive + strafe + turn;
                double lr = drive - strafe + turn;
                double rf = drive - strafe - turn;
                double rr = drive + strafe - turn;
                double max = Math.max(1.0, Math.max(Math.abs(lf), Math.max(Math.abs(lr), Math.max(Math.abs(rf), Math.abs(rr)))));
                leftFront.setPower(lf / max);
                leftRear.setPower(lr / max);
                rightFront.setPower(rf / max);
                rightRear.setPower(rr / max);
            }

            // === РУКА ===
            double armPower = gamepad1.right_bumper ? powerLevel : (gamepad1.left_bumper ? -powerLevel : 0.0);
            catchMotor.setPower(armPower);

            // === ВЫСТРЕЛ ===
            double shootPower = gamepad1.right_trigger > 0.1 ? gamepad1.right_trigger * powerLevel :
                               (gamepad1.left_trigger > 0.1 ? -gamepad1.left_trigger * powerLevel : 0.0);
            extraMotor.setPower(shootPower);
            leftExtra.setPower(shootPower);
            rightExtra.setPower(shootPower);

            // === TELEMETRY ===
            telemetry.clearAll();
            telemetry.addData("SERVO ПОЗИЦИЯ", "%.3f", currentServoPosition);
            telemetry.addData("X/Y", "X=%.1f Y=%.1f", gamepad1.x ? 1.0 : 0.0, gamepad1.y ? 1.0 : 0.0);
            telemetry.addData("Напряжение", "%.1f В", batterySensor.getVoltage());
            telemetry.addData("Рука", "%.2f", armPower);
            telemetry.addData("Выстрел", "%.2f", shootPower);
            telemetry.update();
        }
    }
}