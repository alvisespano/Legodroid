package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;
import android.util.Log;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.util.Prelude;

import java.io.IOException;
import java.util.concurrent.Future;

public class TachoMotor extends Plug<EV3.OutputPort> implements AutoCloseable {
    private static final String TAG = Prelude.ReTAG("TachoMotor");

    public TachoMotor(@NonNull EV3.Api api, EV3.OutputPort port) {
        super(api, port);
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    public Future<Float> getPosition() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_DEGREE, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    public Future<Float> getSpeed() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_SPEED, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    // TODO: lo teniamo o basta resetPosition()?
    public void clearCount() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_CLR_COUNT);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor clear count");
    }

    public void resetPosition() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_RESET);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor reset position");
    }

    public void setSpeed(int speed) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SPEED);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) speed);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor speed set: %d", speed));
    }

    public void setPower(int power) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor power set: %d", power));
    }

    public void start() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_START);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor started");
    }

    public void brake() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.BRAKE);
        api.sendNoReply(bc);
        Log.d(TAG, "motor brake");
    }

    public void stop() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, "motor stop");
    }

    public enum Type {
        MEDIUM, LARGE;

        public byte toByte() {
            switch (this) {
                case MEDIUM:
                    return Const.M_MOTOR;
                default:
                    return Const.L_MOTOR;
            }
        }
    }

    public void setType(Type mt) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SET_TYPE);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toByte());
        bc.addParameter(mt.toByte());
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor type set: %s", mt));
    }

    public enum Polarity {
        BACKWARDS, OPPOSITE, FORWARD;

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

    public void setPolarity(Polarity pol) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SET_TYPE);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toByte());
        bc.addParameter(pol.toByte());
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor polarity set: %s", pol));
    }

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
