package it.unive.dais.legodroid.lib.lowlevel;

import it.unive.dais.legodroid.lib.util.Handler;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Protocol {
    private static final byte OUTPUT_PORT_OFFSET = 0x10;

    public static Promise<float[]> getSiValue(PacketManager manager, int port, int type, int mode, final int nvalue)
            throws IOException {
        ByteCodeGen byteCode = new ByteCodeGen();
        byteCode.addOpCode(Constants.INPUT_DEVICE);
        byteCode.addOpCode(Constants.READY_SI);
        byteCode.addParameter(Constants.LAYER_MASTER);
        byteCode.addParameter((byte) port);
        byteCode.addParameter((byte) type);
        byteCode.addParameter((byte) mode);
        byteCode.addParameter((byte) nvalue);
        byteCode.addGlobalIndex((byte) 0x00);

        final Promise<float[]> returnPromise = new Promise<>();

        Promise<DirectCommandReply> replyPromise = manager.sendPacketAsync(byteCode.getBytes(), 0, 4 * nvalue);
        replyPromise.then(new Handler<DirectCommandReply>() {
            @Override
            public void call(DirectCommandReply data) {
                byte[] reply = data.getData();

                float[] result = new float[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply, 3 + 4 * i, 7 + 4 * i);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }

                returnPromise.resolve(result);
            }
        });

        return returnPromise;
    }

    public static Promise<short[]> getPercentValue(PacketManager manager, int port, int type, int mode,
            final int nvalue) throws IOException {
        ByteCodeGen byteCode = new ByteCodeGen();
        byteCode.addOpCode(Constants.INPUT_DEVICE);
        byteCode.addOpCode(Constants.READY_PCT);
        byteCode.addParameter(Constants.LAYER_MASTER);
        byteCode.addParameter((byte) port);
        byteCode.addParameter((byte) type);
        byteCode.addParameter((byte) mode);
        byteCode.addParameter((byte) nvalue);
        byteCode.addGlobalIndex((byte) 0x00);

        final Promise<short[]> returnPromise = new Promise<>();

        Promise<DirectCommandReply> replyPromise = manager.sendPacketAsync(byteCode.getBytes(), 0, nvalue);
        replyPromise.then(new Handler<DirectCommandReply>() {
            @Override
            public void call(DirectCommandReply data) {
                byte[] reply = data.getData();

                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    result[i] = (short) reply[3 + i];
                }

                returnPromise.resolve(result);
            }
        });

        return returnPromise;
    }

    private byte toByteCodePort(int port) {
        if (port >= 0x00 && port <= 0x03) {
            return (byte) (0x01 << port);
        } else {
            return 0x00;
        }
    }

    public void setOutputState(int port, int speed) {
        ByteCodeGen byteCode = new ByteCodeGen();

        byte byteCodePort = toByteCodePort(port);

        byteCode.addOpCode(Constants.DIRECT_COMMAND_NOREPLY);

        byteCode.addOpCode(Constants.OUTPUT_POWER);
        byteCode.addParameter(Constants.LAYER_MASTER);
        byteCode.addParameter(byteCodePort);
        byteCode.addParameter((byte) speed);

        byteCode.addOpCode(Constants.OUTPUT_START);
        byteCode.addParameter(Constants.LAYER_MASTER);
        byteCode.addParameter(byteCodePort);
    }

    public static void soundTone(PacketManager manager, int volume, int freq, int duration) {
        ByteCodeGen byteCode = new ByteCodeGen();

        byteCode.addOpCode(Constants.DIRECT_COMMAND_NOREPLY);

        byteCode.addOpCode(Constants.SOUND_CONTROL);
        byteCode.addOpCode(Constants.SOUND_TONE);
        byteCode.addParameter((byte) volume);
        byteCode.addParameter((short) freq);
        byteCode.addParameter((short) duration);
    }
}
