package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;
import android.util.Log;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.util.Prelude;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

// TODO: write more details in the javadoc of these methods

/**
 * This class offers methods for controlling the tacho motor of GenEV3 devices.
 */
public class TachoMotor extends Plug<EV3.OutputPort> implements AutoCloseable {
    private static final String TAG = Prelude.ReTAG("TachoMotor");

    /**
     * Constructor.
     *
     * @param api  the object of type {@link EV3.Api}.
     * @param port the output port.
     */
    public TachoMotor(@NonNull EV3.Api api, EV3.OutputPort port) {
        super(api, port);
    }

    @Override
    public void close() {
        Prelude.trap(this::stop);
    }

    /**
     * Get the current position of the motor in tacho ticks.
     *
     * @return the current position of the motor in tacho ticks.
     * @throws IOException thrown when communication errors occur.
     */
    public Future<Float> getPosition() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_DEGREE, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    /**
     * Get the current speed of the motor.
     * Returns the current motor speed in tacho counts per second.
     * Note, this is not necessarily degrees (although it is for LEGO motors).
     *
     * @return the current motor speed in tacho counts per second.
     * @throws IOException thrown when communication errors occur.
     */
    public Future<Float> getSpeed() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_SPEED, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    /**
     * Clear the tacho counter of the motor.
     *
     * @throws IOException thrown when communication errors occur.
     * @deprecated
     */
    @Deprecated
    public void clearCount() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_CLR_COUNT);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor clear count");
    }

    /**
     * Tests whether the motor is busy or not.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public Future<Boolean> isBusy() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_TEST);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        Future<Reply> r = api.send(1, bc);
        Log.d(TAG, "motor is busy");
        return api.execAsync(() -> r.get().getData()[0] != 0);
    }

    /**
     * Wait until the motor is ready.
     * This method blocks the caller thread.
     *
     * @throws IOException thrown when communication errors occur.
     */
     public void waitUntilReady() throws IOException, ExecutionException, InterruptedException {
        while (isBusy().get()) {
            if (isBusy().get()) {
                Log.d(TAG, "motor ready");
                break;
            } else {
                waitCompletion();
            }
        }
    }

    /**
     * Make the GenEV3 wait until the current command has been completed.
     * This method is NOT blocking the caller thread.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public void waitCompletion() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_READY);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor wait until ready");
    }

    /**
     * Reset the position counter of the motor.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public void resetPosition() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_RESET);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor reset position");
    }

    /**
     * Set the speed percentage of the motor.
     * This mode automatically enables speed control, which means the system will automatically adjust the power to keep the specified speed.
     *
     * @param speed the speed percentage in the range [ -100 - 100 ].
     * @throws IOException thrown when communication errors occur.
     */
    public void setSpeed(int speed) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SPEED);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) speed);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor speed set: %d", speed));
    }

    /**
     * Set the power percentage of the motor.
     *
     * @param power the power percentage in the range [ -100 - 100 ].
     * @throws IOException thrown when communication errors occur.
     */
    public void setPower(int power) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor power set: %d", power));
    }

    /**
     * Start the motor.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public void start() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_START);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor started");
    }

    /**
     * Brake the motor.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public void brake() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.BRAKE);
        api.sendNoReply(bc);
        Log.d(TAG, "motor brake");
    }

    /**
     * Stop the motor.
     *
     * @throws IOException thrown when communication errors occur.
     */
    public void stop() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, "motor stop");
    }

    /**
     * Type of motor enumeration type.
     */
    public enum Type {
        /**
         * Medium motor: the small ones, e.g. GenEV3 Medium Servo motor
         */
        MEDIUM,
        /**
         * Large motor: the standard ones, e.g. GenEV3 Large Servo Motor.
         */
        LARGE;

        /**
         * Convert to a byte for use with low level command creation.
         *
         * @return the type as a byte-sized constant.
         */
        public byte toByte() {
            switch (this) {
                case MEDIUM:
                    return Const.M_MOTOR;
                default:
                    return Const.L_MOTOR;
            }
        }
    }

    /**
     * Set the motor type.
     * This is useful for switching mode between different motor types.
     *
     * @param mt the type of the motor.
     * @throws IOException thrown when communication errors occur.
     * @see Type
     */
    public void setType(Type mt) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SET_TYPE);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toByte());
        bc.addParameter(mt.toByte());
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor type set: %s", mt));
    }

    /**
     * Polarity enumeration type.
     */
    public enum Polarity {
        /**
         * Motor will run backward.
         */
        BACKWARDS,
        /**
         * Motor will run opposite direction.
         */
        OPPOSITE,
        /**
         * Motor will run forward.
         */
        FORWARD;

        /**
         * Convert to a byte for use with low level command creation.
         *
         * @return the type as a byte-sized constant.
         */
        public byte toByte() {
            switch (this) {
                case BACKWARDS:
                    return -1;
                case OPPOSITE:
                    return 0;
                default:
                    return 1;
            }
        }
    }

    /**
     * Set the polarity of the tacho motor.
     *
     * @param pol the polarity value.
     * @throws IOException thrown when communication errors occur.
     * @see Polarity
     */
    public void setPolarity(Polarity pol) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_POLARITY);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(pol.toByte());
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor polarity set: %s", pol));
    }

    /**
     * This method enables specifying a full motor power cycle in tacho counts.
     * Step1 specifyes the power ramp up periode in tacho count, Step2 specifyes the constant power period in tacho counts, Step 3 specifyes the power down period in tacho counts.
     *
     * @param power the power level within range [ -100 - 100 ].
     * @param step1 tacho pulses during ramp up.
     * @param step2 tacho pulses during continues run.
     * @param step3 tacho pulses during ramp down.
     * @param brake break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setStepPower(int power, int step1, int step2, int step3, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STEP_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        bc.addParameter(step1);
        bc.addParameter(step2);
        bc.addParameter(step3);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor step power: power=%d, step1=%d, step2=%d, step3=%d, brake=%s", power, step1, step2, step3, brake));
    }

    /**
     * This method enables specifying a full motor power cycle in time.
     * Step1 specifyes the power ramp up periode in milliseconds, Step2 specifyes the constant power period in milliseconds, Step 3 specifyes the power down period in milliseconds.
     *
     * @param power the power level within range [ -100 - 100 ].
     * @param step1 tacho pulses during ramp up.
     * @param step2 tacho pulses during continues run.
     * @param step3 tacho pulses during ramp down.
     * @param brake break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setTimePower(int power, int step1, int step2, int step3, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_TIME_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        bc.addParameter(step1);
        bc.addParameter(step2);
        bc.addParameter(step3);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor time power: power=%d, step1=%d, step2=%d, step3=%d", power, step1, step2, step3));
    }

    /**
     * This method enables specifying a full motor power cycle in tacho counts.
     * The system will automatically adjust the power level to the motor to keep the specified output speed.
     * Step1 specifyes the power ramp up periode in tacho count, Step2 specifyes the constant power period in tacho counts, Step 3 specifyes the power down period in tacho counts.
     *
     * @param speed power level [-100 – 100].
     * @param step1 tacho pulses during ramp up.
     * @param step2 tacho pulses during continues run.
     * @param step3 tacho pulses during ramp down.
     * @param brake break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setStepSpeed(int speed, int step1, int step2, int step3, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STEP_SPEED);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) speed);
        bc.addParameter(step1);
        bc.addParameter(step2);
        bc.addParameter(step3);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor step speed: speed=%d, step1=%d, step2=%d, step3=%d", speed, step1, step2, step3));
    }

    /**
     * This method enables specifying a full motor power cycle in time.
     * The system will automatically adjust the power level to the motor to keep the specified output speed.
     * Step1 specifyes the power ramp up periode in milliseconds, Step2 specifyes the constant power period in milliseconds, Step 3 specifyes the power down period in milliseconds.
     *
     * @param speed power level [-100 – 100].
     * @param step1 tacho pulses during ramp up.
     * @param step2 tacho pulses during continues run.
     * @param step3 tacho pulses during ramp down.
     * @param brake break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setTimeSpeed(int speed, int step1, int step2, int step3, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_TIME_SPEED);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) speed);
        bc.addParameter(step1);
        bc.addParameter(step2);
        bc.addParameter(step3);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor time speed: speed=%d, step1=%d, step2=%d, step3=%d", speed, step1, step2, step3));
    }

    /**
     * This method enables synchonizing two motors.
     * Synchonization should be used when motors should run as synchrone as possible, for example to archieve a model driving straight.
     * Duration is specified in tacho counts.
     * The turn ratio behaves as follows:
     * 0 : Motor will run with same power.
     * 100 : One motor will run with specified power while the other will be close to zero.
     * 200: One motor will run with specified power forward while the other will run in the opposite direction at the same power level.
     *
     * @param power     power level [ -100 - 100 ].
     * @param turnRatio turn ratio [ -200 - 200 ].
     * @param step      tacho pulses (0 = infinite).
     * @param brake     break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setStepSync(int power, int turnRatio, int step, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STEP_SYNC);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        bc.addParameter((short) turnRatio);
        bc.addParameter(step);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor step sync: power=%d, turn=%d, step=%d, brake=%s", power, turnRatio, step, brake));
    }

    /**
     * This method enables synchonizing two motors.
     * Synchonization should be used when motors should run as synchrone as possible, for example to archieve a model driving straight.
     * Duration is specified in time.
     * The turn ratio behaves as follows:
     * 0 : Motor will run with same power.
     * 100 : One motor will run with specified power while the other will be close to zero.
     * 200: One motor will run with specified power forward while the other will run in the opposite direction at the same power level.
     *
     * @param power     power level [ -100 - 100 ].
     * @param turnRatio turn ratio [ -200 - 200 ].
     * @param time      time in milliseconds (0 = infinite).
     * @param brake     break level [false: Float, true: Break].
     * @throws IOException thrown when communication errors occur.
     */
    public void setTimeSync(int power, int turnRatio, int time, boolean brake) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_TIME_SYNC);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        bc.addParameter((short) turnRatio);
        bc.addParameter(time);
        bc.addParameter(brake ? Const.BRAKE : Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor time sync: power=%d, turn=%d, time=%d, brake=%s", power, turnRatio, time, brake));
    }

}
