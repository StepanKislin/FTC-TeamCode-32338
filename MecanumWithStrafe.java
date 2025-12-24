package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.VoltageSensor;

@TeleOp(name = "Mecanum + Стрейф + Extra Motor", group = "Drive")
public class MecanumWithStrafe extends LinearOpMode {

    private static final double TANK_TURN_THRESHOLD = 0.9; // Порог активации танкового поворота
    private static final double TURN_POWER = 0.7;          // Мощность при танковом повороте

    @Override
    public void runOpMode() {
        // Инициализация моторов
        DcMotor leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        DcMotor leftRear   = hardwareMap.get(DcMotor.class, "leftRear");
        DcMotor rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        DcMotor rightRear  = hardwareMap.get(DcMotor.class, "rightRear");
        DcMotor extraMotor = hardwareMap.get(DcMotor.class, "extraMotor");
        DcMotor leftExtra = hardwareMap.get(DcMotor.class, "leftExtra");
        DcMotor rightExtra = hardwareMap.get(DcMotor.class, "rightExtra");
        

        // 🔑 КРИТИЧЕСКИ ВАЖНО: правильные направления для Mecanum!
        leftFront.setDirection(DcMotor.Direction.FORWARD);
        leftRear.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightRear.setDirection(DcMotor.Direction.FORWARD);

        extraMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftExtra.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightExtra.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);



        VoltageSensor batterySensor = hardwareMap.voltageSensor.iterator().next();

        telemetry.addLine("Готов к работе. Проверь направления колёс!");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            double drive  = -gamepad1.left_stick_y;  // вверх = +1
            double strafe = gamepad1.left_stick_x;   // вправо = +1
            double turn   = -gamepad1.right_stick_x; // вправо = +1

            // Проверка на танковый разворот вправо
            boolean tankTurnRight = gamepad1.right_stick_x > TANK_TURN_THRESHOLD;

            if (tankTurnRight) {
                // Танковый поворот направо: левые колёса вперёд, правые — назад
                leftFront.setPower(TURN_POWER);
                leftRear.setPower(TURN_POWER);
                rightFront.setPower(-TURN_POWER);
                rightRear.setPower(-TURN_POWER);
            } else {
                // Обычное mecanum-управление
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

            // Доп. мотор по кнопке X
            extraMotor.setPower(gamepad1.x ? 1.0 : 0.0);
            leftExtra.setPower(gamepad1.y ? 0.5 : 0.0);
            rightExtra.setPower(gamepad1.y ? 0.5 : 0.0);
            

            // Telemetry
            telemetry.addData("Напряжение", "%.1f В", batterySensor.getVoltage());
            telemetry.addData("Стрейф (X)", "%.2f", strafe);
            telemetry.addData("Вперёд (Y)", "%.2f", drive);
            telemetry.addData("Поворот", "%.2f", turn);
            telemetry.addData("Танковый поворот", tankTurnRight);
            telemetry.addData("X нажата", gamepad1.x);
            telemetry.update();
        }
    }
} 
