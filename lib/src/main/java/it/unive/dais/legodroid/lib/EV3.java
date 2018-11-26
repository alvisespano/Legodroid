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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * Instances of this class represent LEGO Mindstorms EV3 bricks: a program shall create one instance of this class for each physical brick it controls.
 * An object of type AsyncChannel is required for calling the constructor, making this class instantiable only once a connection with the brick has been established.
 */
public class EV3 {
    private static final String TAG = ReTAG("EV3");

    /**
     * Specialized exception subclass.
     */
    public static class AlreadyRunningException extends ExecutionException {
        public AlreadyRunningException(String msg) {
            super(msg);
        }
    }

    @NonNull
    private final AsyncChannel channel;
    @Nullable
    private AsyncTask<Void, Void, Void> task = null;

    /**
     * Main constructor.
     *
     * @param channel an asynchronous channel object of type AsyncChannel.
     */
    public EV3(@NonNull AsyncChannel channel) {
        this.channel = channel;
    }

    /**
     * Facility constructor. Creates a SpooledAsyncChannel automatically with the given synchrounous channel
     *
     * @param channel a synchrounous channel object.
     */
    public EV3(@NonNull Channel channel) {
        this(new SpooledAsyncChannel(channel));
    }

    /**
     * Run the given callback as the main program for the EV3 brick.
     * The callback is a function parametric over an object of type Api and returning nothing, hence the Consumer type.
     * Programmers can control the EV3 brick by calling methods of the Api object passed as argument.
     * Notably, there is no other way of getting the Api object for safety reasons.
     * The callback is executed by a worker thread, thus any operation on the UI must be delegated to runOnUiThread() invocations.
     * Also, each EV3 instance can have at most one running task - i.e. one worker thread can be up at any given time.
     *
     * @param f a callback that takes a parameter of type Api and has no return type.
     * @throws AlreadyRunningException thrown when the worker thread is already up.
     */
    public synchronized void run(@NonNull Consumer<Api> f) throws AlreadyRunningException {
        if (task != null)
            throw new AlreadyRunningException("EV3 task is already running");
        task = new MyAsyncTask(this, f).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = ReTAG(EV3.TAG, "AsyncTask");

        @NonNull
        private final EV3 ev3;
        @NonNull
        private final Consumer<Api> f;

        private MyAsyncTask(@NonNull EV3 ev3, @NonNull Consumer<Api> f) {
            this.ev3 = ev3;
            this.f = f;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Thread.currentThread().setName(TAG);
            Log.v(TAG, "starting EV3 task");
            try {
                f.call(new Api(ev3));
                Log.v(TAG, "exiting EV3 task");
            } catch (Throwable e) {
                Log.e(TAG, String.format("uncaught exception: %s. Aborting EV3 task", e.getMessage()));
                e.printStackTrace();
            }
            synchronized (ev3) {
                ev3.task = null;
            }
            return null;
        }
    }

    /**
     * Cancel the EV3 task currently being run by the worker thread.
     * Cancellation is not equivalent to killing the thread, it is just a software flag the task code can peek.
     * This method is thread-safe and can be either called from the callback or from any other thread.
     *
     * @see #isCancelled()
     */
    public synchronized void cancel() {
        if (task != null) {
            Log.v(TAG, "cancelling task");
            task.cancel(true);
        }
    }

    /**
     * Test the cancellation flag. This is meant to be called from within the EV3 task callback.
     * @return returns true when a call to cancel() has been performed.
     *
     * @see #cancel()
     */
    public synchronized boolean isCancelled() {
        return task == null || task.isCancelled();
    }

    /**
     * This class contains the operations that can be performed to control the EV3 brick.
     * Accesing sensors, moving motors etc. are operations that can be performed only by calling methods of this class.
     * Instances of this class cannot be created by calling a constructor: an instance can only be obtained from the argument of the callback of type
     * {@code Consumer<Api>} passed to method {@code EV 3::run()}.
     */
    public static class Api {
        @NonNull
        public final EV3 ev3;

        private Api(@NonNull EV3 ev3) {
            this.ev3 = ev3;
        }

        /**
         * Access the light sensor of EV3.
         * @param port the input port where the light sensor is connected to on the brick.
         * @return an object of type LightSensor.
         */
        @NonNull
        public LightSensor getLightSensor(InputPort port) {
            return new LightSensor(this, port);
        }

        /**
         * Access the touch sensor of EV3.
         * @param port the input port where the touch sensor is connected to on the brick.
         * @return an object of type TouchSensor.
         */
        @NonNull
        public TouchSensor getTouchSensor(InputPort port) {
            return new TouchSensor(this, port);
        }

        /**
         * Access the ultrasonic sensor of EV3.
         * @param port the input port where the ultrasonic sensor is connected to on the brick.
         * @return an object of type UltrasonicSensor.
         */
        @NonNull
        public UltrasonicSensor getUltrasonicSensor(InputPort port) {
            return new UltrasonicSensor(this, port);
        }

        /**
         * Access the gyroscope sensor of EV3.
         * @param port the input port where the gyroscope sensor is connected to on the brick.
         * @return an object of type GyroSensor.
         */
        @NonNull
        public GyroSensor getGyroSensor(InputPort port) {
            return new GyroSensor(this, port);
        }

        /**
         * Access the tacho motor of EV3.
         * @param port the output port where the motor is connected to on the brick.
         * @return an object of type TachoMotor.
         */
        @NonNull
        public TachoMotor getTachoMotor(OutputPort port) {
            return new TachoMotor(this, port);
        }

        /**
         * Play a sound tone on the EV3 brick
         * @param volume volume in range 
         * @param freq
         * @param duration
         * @throws IOException
         */
        public void soundTone(int volume, int freq, int duration) throws IOException {
            Bytecode bc = new Bytecode();
            bc.addOpCode(Const.SOUND_CONTROL);
            bc.addOpCode(Const.SOUND_TONE);
            bc.addParameter((byte) volume);
            bc.addParameter((short) freq);
            bc.addParameter((short) duration);
            ev3.channel.sendNoReply(bc);
        }

        // low level API
        //

        private final Executor executor = Executors.newSingleThreadExecutor();

        @NonNull
        private Bytecode prefaceGetValue(byte ready, byte port, int type, int mode, int nvalue) throws IOException {
            Bytecode r = new Bytecode();
            r.addOpCode(Const.INPUT_DEVICE);
            r.addOpCode(ready);
            r.addParameter(Const.LAYER_MASTER);
            r.addParameter(port);
            r.addParameter((byte) type);
            r.addParameter((byte) mode);
            r.addParameter((byte) nvalue);
            r.addGlobalIndex((byte) 0x00);
            return r;
        }

        @NonNull
        public Future<float[]> getSiValue(byte port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = prefaceGetValue(Const.READY_SI, port, type, mode, nvalue);
            Future<Reply> r = ev3.channel.send(4 * nvalue, bc);
            return execAsync(() -> {
                Reply reply = r.get();
                float[] result = new float[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply.getData(), 4 * i, 4 * i + 4);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }
                return result;
            });
        }

        @NonNull
        public <T> FutureTask<T> execAsync(@NonNull Callable<T> c) {
            FutureTask<T> t = new FutureTask<>(c);
            executor.execute(t);
            return t;
        }

        @NonNull
        public Future<short[]> getPercentValue(byte port, int type, int mode, int nvalue) throws IOException {
            Bytecode bc = prefaceGetValue(Const.READY_PCT, port, type, mode, nvalue);
            Future<Reply> fr = ev3.channel.send(2 * nvalue, bc);
            return execAsync(() -> {
                Reply r = fr.get();
                byte[] reply = r.getData();
                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply, 2 * i, 2 * i + 2);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getShort();
                }
                return result;
            });
        }

        public Future<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException {
            return ev3.channel.send(reservation, bc);
        }

        public void sendNoReply(Bytecode bytecode) throws IOException {
            ev3.channel.sendNoReply(bytecode);
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
                default:
                    return 3;
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        @NonNull
        public String toString() {
            return String.format("In/%d", toByte());
        }
    }

    public enum OutputPort {
        A, B, C, D;

        public byte toBitmask() {
            return (byte) (1 << toByte());
        }

        public byte toByteAsRead() {
            return (byte) (toByte() | 0x10);
        }

        public byte toByte() {
            switch (this) {
                case A:
                    return 0;
                case B:
                    return 1;
                case C:
                    return 2;
                default:
                    return 3;
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        @NonNull
        public String toString() {
            return String.format("Out/%s", super.toString());
        }
    }

}
