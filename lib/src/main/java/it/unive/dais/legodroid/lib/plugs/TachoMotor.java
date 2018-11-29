package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;
import android.util.Log;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.util.Prelude;

import java.io.IOException;
import java.util.concurrent.Future;

// TODO: write more details in the javadoc of these methods

/**
 * This class offers methods for controlling the tacho motor of EV3 devices.
 */
public class TachoMotor extends Plug<EV3.OutputPort> implements AutoCloseable {
    private static final String TAG = Prelude.ReTAG("TachoMotor");

    /**
     * Constructor. Internal use only.
     *
     * @param api  the object of type {@link it.unive.dais.legodroid.lib.EV3.Api}.
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
     * @return the current position of the motor in tacho ticks as float.
     * @throws IOException thrown when communication errors occur.
     */
    public Future<Float> getPosition() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_DEGREE, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    /**
     * Get the current speed of the motor.
     *
     * @return the current position of the motor in tacho ticks as float.
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
     */
    public void clearCount() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_CLR_COUNT);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor clear count");
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
     * Set the speed of the motor.
     *
     * @param speed the speed in the range [ -100 - 100 ].
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
     * Set the power of the motor.
     *
     * @param power the speed in the range [ -100 - 100 ].
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
        MEDIUM, LARGE;

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
     *
     * @param mt the type of the motor.
     * @throws IOException thrown when communication errors occur.
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
        BACKWARDS, OPPOSITE, FORWARD;

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
     * @param pol the polarity value.
     * @throws IOException thrown when communication errors occur.
     */
    public void setPolarity(Polarity pol) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SET_TYPE);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toByte());
        bc.addParameter(pol.toByte());
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor polarity set: %s", pol));
    }

    /**
     * Set the step power.
     * @param power the power within range [ -100 - 100 ].
     * @param step1 the step
     * @param step2
     * @param step3
     * @param brake
     * @throws IOException
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

    public void stepSync(int power, int turnRatio, int step, boolean brake) throws IOException {
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

    public void timeSync(int power, int turnRatio, int time, boolean brake) throws IOException {
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
