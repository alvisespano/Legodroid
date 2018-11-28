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
     * @param f functional object that takes a parameter of type Api and has no return type.
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
     * @see #cancel()
     */
    public synchronized boolean isCancelled() {
        return task == null || task.isCancelled();
    }

    /**
     * This inner class contains the operations that can be performed to control the EV3 brick.
     * Accesing sensors, moving motors etc. are operations that can be performed only by calling methods of this class.
     * Instances of this class cannot be created by calling a constructor: an instance can only be obtained from the argument of the callback passed to method {@link #run(Consumer)}.
     * Methods offered by this class belong to two categories: sensor and motor methods are high level operations, though users can directly send low-level
     * commands to the EV3 brick by calling {@link #getSiValue(byte, int, int, int)} and {@link #getPercentValue(byte, int, int, int)} methods.
     * @see #getSiValue(byte, int, int, int)
     * @see #getPercentValue(byte, int, int, int)
     */
    public static class Api {
        /**
         * Public field pointing to the EV3 object.
         */
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
         * Play a sound tone on the EV3 brick.
         * @param volume volume within the range [0 - 100].
         * @param freq frequency in the range [ 250 - 10000 ].
         * @param duration duration in milliseconds.
         * @throws IOException thown when communication errors occur.
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

        /**
         * Low level method for sending direct commands to the EV3 brick.
         * This method sends the opInput_Device command for reading SI values according to the official EV3 Developer Kit Documentation.
         * @param port port number.
         * @param type type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 32-bit floats whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
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

        /**
         * Low level method for sending direct commands to the EV3 brick.
         * This method sends the opInput_Device command for reading PCT values according to the official EV3 Developer Kit Documentation.
         * @param port port number.
         * @param type type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 16-bit integers whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
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

        /**
         * Low level method to execute the callback passed as argument within an Android {@link FutureTask}.
         * The {@link Executor} in charge of the executing the function the is a builtin single-threaded executor.
         * @param c functional object of type {@link Callable}.
         * @param <T> the return type of the callback.
         * @return a {@link FutureTask} object hosting the result of type {@code T}.
        */
        @NonNull
        public <T> FutureTask<T> execAsync(@NonNull Callable<T> c) {
            FutureTask<T> t = new FutureTask<>(c);
            executor.execute(t);
            return t;
        }

        /**
         * Low level send command with reply.
         * @param reservation global reservation for the result in bytes.
         * @param bc object of type {@link Bytecode} representing the command to be sent.
         * @return a {@link Future} object hosting the {@link Reply} object wrapping the reply by EV3.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public Future<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException {
            return ev3.channel.send(reservation, bc);
        }

        /**
         * Low level send command with no reply.
         * @param bc object of type {@link Bytecode} representing the command to be sent.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public void sendNoReply(Bytecode bc) throws IOException {
            ev3.channel.sendNoReply(bc);
        }
    }

    /**
     * This enum type represents the 4 physical input ports on the EV3 brick.
     */
    public enum InputPort {
        /**
         * Input port 1
         */
        _1,
        /**
         * Input port 2
         */
        _2,
        /**
         * Input port 3
         */
        _3,
        /**
         * Input port 4
         */
        _4;

        /**
         * Encode the input port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         * @return a byte according to the encoding defined by the EV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
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

    /**
     * This enum type represents the 4 physical output ports on the EV3 brick.
     */
    public enum OutputPort {
        /**
         * Output port A
         */
        A,
        /**
         * Output port B
         */
        B,
        /**
         * Output port C
         */
        C,
        /**
         * Output port D
         */
        D;

        /**
         * Encode the output port as a bit mask for certain EV3 direct commands that require the bitmask format as parameter.
         * @return a byte with the bit mask.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public byte toBitmask() {
            return (byte) (1 << toByte());
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         * Using output ports for read operations is possible, though a special encoding is needed according to the EV3 Developer Kit Documentation - this is provided by this method.
         * @return a byte according to the encoding defined by the EV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public byte toByteAsRead() {
            return (byte) (toByte() | 0x10);
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         * @return a byte according to the encoding defined by the EV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
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
