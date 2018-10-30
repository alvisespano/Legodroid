package it.unive.dais.legodroid.lib;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.GyroSensor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.sensors.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.UnexpectedException;

public class EV3 {
    private static final String TAG = "EV3";
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

    @SuppressLint("StaticFieldLeak")
    public void run(@NonNull Consumer<Api> c) {
        new AsyncTask<Void, Void, Void>() {
            private static final String TAG = "EV3Worker";

            @Override
            protected Void doInBackground(Void... voids) {
                Thread.currentThread().setName(TAG);
                try {
                    c.call(new Api());
                }
                catch (Exception e) {
                    Log.e(TAG, String.format("uncaught exception: %s. Aborting EV3 job.", e.getMessage()));
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendEvent(Event e) {
        incomingEvents.add(e);
    }

    public void setEventListener(@NonNull Consumer<Event> eventListener) {
        this.eventListener = eventListener;
    }

    public enum InputPort {
        _1, _2, _3, _4;

        public byte toByte() {
            switch (this) {
                case _1: return 0;
                case _2: return 1;
                case _3: return 2;
                case _4: return 3;
            }
            throw new UnexpectedException("invalid input port");
        }
    }

    public enum OutputPort {
        A, B, C, D;

        public byte toByte() {
            switch (this) {
                case A: return 0;
                case B: return 1;
                case C: return 2;
                case D: return 3;
            }
            throw new UnexpectedException("invalid output port");
        }
    }

    public class Api {

        public LightSensor getLightSensor(InputPort port) {
            return new LightSensor(this, port);
        }

        public TouchSensor getTouchSensor(InputPort port) {
            return new TouchSensor(this, port);
        }

        public UltrasonicSensor getUltrasonicSensor(InputPort port) {
            return new UltrasonicSensor(this, port);
        }

        public GyroSensor getGyroSensor(InputPort port) {
            return new GyroSensor(this, port);
        }

        public TachoMotor getTachoMotor(OutputPort port) {
            return new TachoMotor(this, port);
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

        private Bytecode preface(byte ready, InputPort port, int type, int mode, int nvalue) throws IOException {
            Bytecode r = new Bytecode();
            r.addOpCode(Const.INPUT_DEVICE);
            r.addOpCode(ready);
            r.addParameter(Const.LAYER_MASTER);
            r.addParameter(port.toByte());
            r.addParameter((byte) type);
            r.addParameter((byte) mode);
            r.addParameter((byte) nvalue);
            r.addGlobalIndex((byte) 0x00);
            return r;
        }

        // TODO: controllare che la manipolazione byte a byte sia corretta per tutti questi metodi che operano a basso livello

        public Future<float[]> getSiValue(InputPort port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = preface(Const.READY_SI, port, type, mode, nvalue);
            Future<Reply> r = channel.send(4 * nvalue, bc);
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

        public FutureTask<short[]> getPercentValue(InputPort port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = preface(Const.READY_PCT, port, type, mode, nvalue);
            SpooledAsyncChannel.MyFuture r = channel.send(2 * nvalue, bc);
            return new FutureTask<>(() -> {
                byte[] reply = r.get().getData();
                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    result[i] = (short) reply[i];
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
