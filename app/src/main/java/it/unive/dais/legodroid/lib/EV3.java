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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.GyroSensor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.sensors.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.UnexpectedException;

public class EV3 {
    private static final String TAG = "EV3";

    // base type for events: inherit this to define your own event types
    public interface Event {}

    @NonNull
    private final AsyncChannel channel;
    @Nullable
    private Consumer<Event> eventListener;
    @NonNull
    private final Queue<Event> incomingEvents = new ConcurrentLinkedQueue<>();
    @Nullable
    private AsyncTask<Void, Void, Void> task;

    public EV3(@NonNull AsyncChannel channel) {
        this.channel = channel;
    }

    @SuppressLint("StaticFieldLeak")
    public void run(@NonNull Consumer<Api> c) {
        task = new AsyncTask<Void, Void, Void>() {
            private static final String TAG = "EV3-Task";

            @Override
            protected Void doInBackground(Void... voids) {
                Thread.currentThread().setName(TAG);
                try {
                    c.call(new Api());
                } catch (Exception e) {
                    Log.e(TAG, String.format("uncaught exception: %s. Aborting EV3 task.", e.getMessage()));
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                task = null;
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendEvent(Event e) {
        incomingEvents.add(e);
    }

    public void setEventListener(@NonNull Consumer<Event> eventListener) {
        this.eventListener = eventListener;
    }

    public void cancel() {
        if (task != null) {
            Log.d(TAG, "cancelling task");
            task.cancel(true);
        }
    }

    public boolean isCancelled() {
        if (task != null) {
            return task.isCancelled();
        }
        else return true;
    }

    public class Api {

        public EV3 getEV3() { return EV3.this; }

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

        public synchronized Event pollEvents() {
            return incomingEvents.poll();
        }

        public synchronized void sendEvent(Event e) {
            if (eventListener != null) {
                eventListener.call(e);
            }
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

        // low level API
        //

        private Executor executor = Executors.newSingleThreadExecutor();

        private Bytecode prefaceGetValue(byte ready, InputPort port, int type, int mode, int nvalue) throws IOException {
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

        public Future<float[]> getSiValue(InputPort port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = prefaceGetValue(Const.READY_SI, port, type, mode, nvalue);
            Future<Reply> r = channel.send(4 * nvalue, bc);
            return execAsync(() -> {
                Reply reply = r.get();
                float[] result = new float[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply.getData(), 3 + 4 * i, 7 + 4 * i);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }
                return result;
            });
        }

        public <T> FutureTask<T> execAsync(Callable<T> c) {
            FutureTask<T> t = new FutureTask<>(c);
            executor.execute(t);
            return t;
        }

        public Future<short[]> getPercentValue(InputPort port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = prefaceGetValue(Const.READY_PCT, port, type, mode, nvalue);
            Future<Reply> fr = channel.send(2 * nvalue, bc);
            return execAsync(() -> {
                Reply r = fr.get();
                byte[] reply = r.getData();
                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    result[i] = (short) reply[i];
                }
                return result;
            });
        }

        public void setOutputSpeed(OutputPort port, int speed) throws IOException {
            Bytecode bc = new Bytecode();
            byte p = (byte) (0x01 << port.toByte());
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

    public enum InputPort {
        _1, _2, _3, _4;

        public byte toByte() {
            switch (this) {
                case _1:
                    return 0;
                case _2:
                    return 1;
                case _3:
                    return 2;
                case _4:
                    return 3;
            }
            throw new UnexpectedException("invalid input port");
        }
    }

    public enum OutputPort {
        A, B, C, D;

        public byte toByte() {
            switch (this) {
                case A:
                    return 0;
                case B:
                    return 1;
                case C:
                    return 2;
                case D:
                    return 3;
            }
            throw new UnexpectedException("invalid output port");
        }
    }
}
