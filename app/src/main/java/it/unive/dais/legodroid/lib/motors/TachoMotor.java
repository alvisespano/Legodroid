package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Future;

public class TachoMotor {
    private final EV3.Api api;
    private final EV3.OutputPort port;

    public TachoMotor(EV3.Api api, EV3.OutputPort port) {
        this.api = api;
        this.port = port;
    }

    public Future<Float> getPosition() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.INPUT_DEVICE);
        bc.addOpCode(Const.READY_SI);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(Const.OUTPUT_PORT_OFFSET | port.toByte());
        bc.addParameter(Const.L_MOTOR);
        bc.addParameter(Const.L_MOTOR_DEGREE);
        bc.addParameter((byte) 1);
        bc.addGlobalIndex((byte) 0x00);
        Future<Reply> r = api.send(4, bc);
        return api.execAsync(() -> {
            Reply reply = r.get();
            float result;
            byte[] bData = Arrays.copyOfRange(reply.getData(), 3 + 4, 7 + 4);
            result = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return result;
        });
    }

    public void resetPosition() throws IOException {
        Bytecode bc = new Bytecode();
        byte p = (byte) (0x01 << port.toByte());
        bc.addOpCode(Const.OUTPUT_RESET);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(p);
        api.sendNoReply(bc);
    }

    public boolean isStalled() {
        return false;
    }

    public void goToPositionRel(int amount) {

    }

    public void goToPositionAbs(int pos) {

    }

    public void setSpeed(int speed) throws IOException {
        Bytecode bc = new Bytecode();
        byte p = (byte) (0x01 << port.toByte());
        bc.addOpCode(Const.OUTPUT_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(p);
        bc.addParameter((byte) speed);
        api.sendNoReply(bc);
    }

    public void brake() throws IOException {
        Bytecode bc = new Bytecode();
        byte p = (byte) (0x01 << port.toByte());
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(p);
        bc.addParameter(Const.BRAKE);
        api.sendNoReply(bc);
    }

    public void stop() throws IOException {
        Bytecode bc = new Bytecode();
        byte p = (byte) (0x01 << port.toByte());
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(p);
        bc.addParameter(Const.COAST);
        api.sendNoReply(bc);
    }

    public boolean isMoving() { // TODO: questo non è un doppione con isStill()?
        return false;
    }
}
