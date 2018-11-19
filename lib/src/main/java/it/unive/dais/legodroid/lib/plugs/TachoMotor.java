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

    public void resetPosition() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_RESET);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
    }

    public boolean isStalled() {
        return false;
    }

    public void goToPositionRel(int amount) {
        // TODO
    }

    public void goToPositionAbs(int pos) {
        // TODO
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

    public boolean isMoving() { // TODO: questo non è un doppione con isStill()?
        return false;
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


    // TODO: serve davvero?
//    public Pair<> getSpeed() {
//        Bytecode bc = new Bytecode();
//        bc.addOpCode(Const.OUTPUT_READ);
//        bc.addParameter(Const.LAYER_MASTER);
//        bc.addParameter(port.toByte());
//        bc.addParameter(0);
//        bc.addParameter(0);
//        api.sendNoReply(bc);
//        Log.d(TAG, String.format("motor polarity set: %s", pol));
//    }

}
