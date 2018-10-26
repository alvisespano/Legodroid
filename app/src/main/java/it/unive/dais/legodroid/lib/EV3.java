package it.unive.dais.legodroid.lib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Command;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.GyroSensor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.sensors.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

public class EV3 {
    @NonNull
    private final AsyncChannel channel;
    @Nullable
    private Consumer<Event> eventListener;
    @NonNull
    private final Queue<Event> incomingEvents = new ConcurrentLinkedQueue<>();

    public interface Event {
    }

    public EV3(@NonNull AsyncChannel channel) {
        this.channel = channel;
    }

    public void run(Consumer<Api> c) {
        c.call(new Api());
    }

    public void sendEvent(Event e) {
        incomingEvents.add(e);
    }

    public void setEventListener(@NonNull Consumer<Event> eventListener) {
        this.eventListener = eventListener;
    }

    public class Api {
        private final EV3 ev3 = EV3.this;

        public LightSensor getLightSensor(int port) {
            return new LightSensor(ev3, port);
        }

        public TouchSensor getTouchSensor(int port) {
            return new TouchSensor(ev3, port);
        }

        public UltrasonicSensor getUltrasonicSensor(int port) {
            return new UltrasonicSensor(ev3, port);
        }

        public GyroSensor getGyroSensor(int port) {
            return new GyroSensor(ev3, port);
        }

        public TachoMotor getTachoMotor(int port) {
            return new TachoMotor(ev3, port);
        }

        public Event pollEvents() {
            return incomingEvents.poll();
        }

        public void sendEvent(Event e) {
            if (eventListener != null) {
                eventListener.call(e);
            }
        }

        // low level API
        //

        private Bytecode preface(byte ready, int port, int type, int mode, int nvalue) throws IOException {
            Bytecode r = new Bytecode();
            r.addOpCode(Const.INPUT_DEVICE);
            r.addOpCode(ready);
            r.addParameter(Const.LAYER_MASTER);
            r.addParameter((byte) port);
            r.addParameter((byte) type);
            r.addParameter((byte) mode);
            r.addParameter((byte) nvalue);
            r.addGlobalIndex((byte) 0x00);
            return r;
        }

        // TODO: controllare che la manipolazione byte a byte sia corretta per tutti questi metodi che operano a basso livello

        public Future<float[]> getSiValue(int port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = preface(Const.READY_SI, port, type, mode, nvalue);
            Command cmd = new Command(true, 0, 4 * nvalue, bc.getBytes());
            Future<Reply> r = channel.send(cmd);
            return new FutureTask<>(() -> {
                Reply reply = r.get();
                float[] result = new float[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply.getData(), 3 + 4 * i, 7 + 4 * i);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }
                return result;
            });
        }

        public Future<short[]> getPercentValue(int port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = preface(Const.READY_PCT, port, type, mode, nvalue);
            Command cmd = new Command(true, 0, 4 * nvalue, bc.getBytes());  // TODO: questo 4 * nvalue è giusto anche se sono short?
            Future<Reply> r = channel.send(cmd);
            return new FutureTask<>(() -> {
                byte[] reply = r.get().getData();
                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    result[i] = (short) reply[3 + i];
                }
                return result;
            });
        }

        public void soundTone(int volume, int freq, int duration) throws IOException {
            Bytecode bc = new Bytecode();
            bc.addOpCode(Const.SOUND_CONTROL);
            bc.addOpCode(Const.SOUND_TONE);
            bc.addParameter((byte) volume);
            bc.addParameter((short) freq);
            bc.addParameter((short) duration);
            channel.sendNoReply(bc);
        }

        private byte toByteCodePort(int port) {
            if (port >= 0x00 && port <= 0x03) {
                return (byte) (0x01 << port);
            } else {
                return 0x00;
            }
        }

        public void setOutputState(int port, int speed) throws IOException {
            Bytecode bc = new Bytecode();
            byte p = toByteCodePort(port);
            bc.addOpCode(Const.OUTPUT_POWER);
            bc.addParameter(Const.LAYER_MASTER);
            bc.addParameter(p);
            bc.addParameter((byte) speed);
            bc.addOpCode(Const.OUTPUT_START);
            bc.addParameter(Const.LAYER_MASTER);
            bc.addParameter(p);
            channel.sendNoReply(bc);
        }

    }

}
