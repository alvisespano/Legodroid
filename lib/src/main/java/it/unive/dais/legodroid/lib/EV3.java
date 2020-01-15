package it.unive.dais.legodroid.lib;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.Command;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Function;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * Instances of this class represent LEGO Mindstorms EV3 bricks: a program shall create one instance of this class for each physical brick it controls.
 * An object of type {@link AsyncChannel} or {@link Channel} is required for calling the constructor, making this class instantiable only once a connection with the brick has been established.
 */
public class EV3 {
    private static final String TAG = ReTAG("EV3");

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
         *
         * @return a byte according to the encoding defined by the EV3 Developer Kit Documentation.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms EV3 Firmware Developer Kit</a>
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
         *
         * @return a byte with the bit mask.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public byte toBitmask() {
            return (byte) (1 << toByte());
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         * Using output ports for receive operations is possible, though a special encoding is needed according to the EV3 Developer Kit Documentation - this is provided by this method.
         *
         * @return a byte according to the encoding defined by the EV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">EV3 Developer Kit Documentation</a>
         */
        public byte toByteAsRead() {
            return (byte) (toByte() | 0x10);
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         *
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

        // TODO: implementare il comando opOutput_Test che verifica se le output port sono occupate
    }

    /**
     * Specialized exception subclass.
     */
    public static class AlreadyRunningException extends ExecutionException {
        public AlreadyRunningException(String msg) {
            super(msg);
        }
    }

    @NonNull
    protected final static Set<AsyncChannel> channels = new HashSet<>();
    @NonNull
    protected final AsyncChannel<?> channel;
    @Nullable
    protected AsyncTask<Void, Void, Void> task = null;

    /**
     * Create a EV3 object given the underlying {@link AsyncChannel}.
     *
     * @param channel an asynchronous channel object of type AsyncChannel.
     */
    public EV3(@NonNull AsyncChannel<?> channel) throws AlreadyRunningException {
        this.channel = channel;
        if (channels.contains(channel))
            throw new AlreadyRunningException(String.format("channel '%s' is already in use", channel.toString()));
        channels.add(channel);
    }

    /**
     * Create a EV3 object given the underlying {@link Channel} by making it asynchronous through a spooler background task.
     *
     * @param channel a synchrounous channel object.
     */
    public <P> EV3(@NonNull Channel<P> channel) throws AlreadyRunningException {
        this(new SpooledAsyncChannel<>(channel));
    }

    /**
     * Run the legoMain callback as the main program for the EV3 brick.
     * The callback is a function parametric over an object of type A and returning nothing, hence the Consumer type.
     * Programmers can control the EV3 brick by calling methods of the Api object passed as argument.
     * Notably, there is no other way of getting the Api object for safety reasons.
     * The callback is executed by a worker thread, thus any operation on the UI must be delegated to runOnUiThread() invocations.
     * Also, each EV3 instance can have at most one running task - i.e. one worker thread can be up at any given time.
     *
     * @param legoMain functional object that takes a parameter of type Api and has no return type.
     * @param makeApi  functional object that constructs an Api object given an EV3 object; the return Api object is passed as argument to the legoMain function object.
     * @throws AlreadyRunningException thrown when the worker thread is already up.
     */
    public synchronized <A extends Api> void run(@NonNull Consumer<? super A> legoMain, @NonNull Function<EV3, A> makeApi) throws AlreadyRunningException {
        if (task != null)
            throw new AlreadyRunningException(String.format("this EV3 instance already has a running background task over channel '%s'", channel.toString()));
        task = new SpoolerAsyncTask<>(this, legoMain, makeApi).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Run the given callback as the main program for the EV3 brick.
     *
     * @param f the function object representing the main code for the EV3 brick.
     * @throws AlreadyRunningException thrown when a program is already running on the EV3 brick.
     */
    public void run(@NonNull Consumer<Api> f) throws AlreadyRunningException {
        run(f, Api::new);
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("EV3[AsyncChannel=%s BackgroundTask=%s]", channel, task != null ? "running" : "none");
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }

    // internal async task
    private static class SpoolerAsyncTask<A extends Api> extends AsyncTask<Void, Void, Void> {
        private final String TAG = ReTAG(EV3.TAG, "AsyncTask");

        @NonNull
        private final EV3 ev3;
        @NonNull
        private final Consumer<? super A> main;
        @NonNull
        private final Function<EV3, A> make;

        private SpoolerAsyncTask(@NonNull EV3 ev3, @NonNull Consumer<? super A> main, @NonNull Function<EV3, A> make) {
            this.ev3 = ev3;
            this.main = main;
            this.make = make;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Thread.currentThread().setName(TAG);
            Log.v(TAG, "starting EV3 task");
            try {
                main.call(make.apply(ev3));
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
     *
     * @return returns true when a call to cancel() has been performed.
     * @see #cancel()
     */
    public synchronized boolean isCancelled() {
        return task == null || task.isCancelled();
    }

    /**
     * This class implements an asynchronous channel that sends commands and receives replies via a spooler thread.
     *
     * @see AsyncChannel
     */
    private static class SpooledAsyncChannel<P> implements AsyncChannel<P> {

        @NonNull
        private final Channel<P> channel;
        @NonNull
        private final List<FutureReply> q = Collections.synchronizedList(new ArrayList<>());
        @NonNull
        private final SpoolerTask task;

        /**
         * Create an asynchronous channel given a synchrounous channel.
         *
         * @param channel a synchrounous channel.
         */
        public SpooledAsyncChannel(@NonNull Channel<P> channel) {
            this.channel = channel;
            this.task = new SpoolerTask(channel, q);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void close() {
            task.cancel(true);
        }

        @NonNull
        @Override
        public P getPeer() {
            return channel.getPeer();
        }

        @Override
        @NonNull
        public String toString() {
            return String.format("SpooledAsyncChannel[%s]", channel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channel);
        }

        private static class SpoolerTask extends AsyncTask<Void, Void, Void> {
            private static final String TAG = ReTAG("SpoolerTask");
            private static final int MAX_RETRIES = 5;

            @NonNull
            private final Channel<?> channel;
            @NonNull
            private final List<FutureReply> q;

            private SpoolerTask(@NonNull Channel<?> ch, @NonNull List<FutureReply> q) {
                this.channel = ch;
                this.q = q;
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected Void doInBackground(Void... voids) {
                Log.v(TAG, "spooler task started");
                Thread.currentThread().setName(TAG);
                int retries = MAX_RETRIES;
                @NonNull String cause = "cancellation";
                @Nullable Throwable last = null;
                while (!isCancelled()) {
                    try {
                        Reply r = channel.receive();
                        synchronized (q) {
                            for (FutureReply t : q) {
                                if (t.id == r.getCounter()) {
                                    t.setReply(r);
                                    break;
                                }
                            }
                        }
                        retries = MAX_RETRIES;
                    } catch (Throwable e) {
                        Log.e(TAG, String.format("recoverable exception caught: %s", e));
                        e.printStackTrace();
                        if (e.equals(last)) {
                            if (retries-- > 0)
                                Log.e(TAG, String.format("retries left: %d", retries));
                            else {
                                cause = String.format("max retries (%d) reached for exception %s", MAX_RETRIES, e.getMessage());
                                break;
                            }
                        } else retries = MAX_RETRIES;
                        last = e;
                    }
                }
                Log.v(TAG, String.format("spooler task quitting due to %s", cause));
                return null;
            }
        }

        /**
         * This class implements a {@link Future} over a {@link Reply} object specifically tailored for the {@link SpooledAsyncChannel} outer class.
         * Access is thread-safe and the hosted reply is <b>stored once</b> and returned at each call of the {@link #get()} method.
         * Calls to {@link #get()} and {@link #get(long, TimeUnit)} are <b>blocking</b> when the reply is yet to be received; subsequent calls return immediately.
         * Cancellation is not supported.
         *
         * @see Future
         */
        private static class FutureReply implements Future<Reply> {
            private static final long GET_MAX_TIMEOUT_MS = 30000;
            private final int id;
            @NonNull
            private final Lock lock = new ReentrantLock();
            @NonNull
            private final Condition cond = lock.newCondition();
            @Nullable
            private Reply reply = null;
            private boolean waiting;

            private FutureReply(int id) {
                this.id = id;
            }

            private void setReply(@Nullable Reply r) {
                lock.lock();
                try {
                    reply = r;
                    cond.signalAll();
                } finally {
                    lock.unlock();
                }
            }

            /**
             * Attempt at cancelling the blocking wait performed by {@link #get(long, TimeUnit)} and {@link #get()}.
             *
             * @param b may interrupt.
             * @return always false.
             * @implNote Cancellation is not supported.
             */
            @Override
            public boolean cancel(boolean b) {
                return false;
            }

            /**
             * Tests if wait has been cancelled
             *
             * @return always false.
             * @implNote Cancellation is not supported.
             */
            @Override
            public boolean isCancelled() {
                return false;
            }

            /**
             * Check whether the future hosts the internal reply.
             *
             * @return true when the reply has been stored.
             */
            @Override
            public boolean isDone() {
                lock.lock();
                try {
                    return reply != null;
                } finally {
                    lock.unlock();
                }
            }

            /**
             * Get the reply with the default timeout (30 seconds).
             * This method is <b>blocking</b> when the reply is yet to be received; subsequent calls return immediately.
             *
             * @return the {@link Reply} object.
             * @throws InterruptedException thrown when interrupted.
             */
            @Override
            @NonNull
            public Reply get() throws InterruptedException, ExecutionException {
                try {
                    return get(GET_MAX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new ExecutionException(e);
                }
            }

            /**
             * Get the reply with the given timeout.
             * This method is <b>blocking</b> when the reply is yet to be received; subsequent calls return immediately.
             *
             * @param l        amount of time units to wait.
             * @param timeUnit the time unit.
             * @return the {@link Reply} object.
             * @throws InterruptedException thrown when interrupted.
             */
            @NonNull
            @Override
            public Reply get(long l, @NonNull TimeUnit timeUnit) throws InterruptedException, TimeoutException {
                lock.lock();
                try {
                    if (reply == null)
                        cond.await(l, timeUnit);
                    if (reply == null) throw new TimeoutException(String.format("FutureReply.get() timed out (%d %s)", l, timeUnit));
                    return reply;
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        @NonNull
        public FutureReply send(@NonNull Command cmd) throws IOException {
            channel.send(cmd);
            FutureReply r = new FutureReply(cmd.getCounter());
            q.add(r);
            return r;
        }

        @NonNull
        @Override
        public FutureReply send(int reservation, @NonNull Bytecode bc) throws IOException {
            return send(new Command(true, 0, reservation, bc.getBytes()));
        }

        @Override
        public void sendNoReply(@NonNull Bytecode bc) throws IOException {
            channel.send(new Command(false, 0, 0, bc.getBytes()));
        }

    }

    /**
     * This inner class contains the operations that can be performed to control the EV3 brick.
     * Accesing sensors, moving motors etc. are operations that can be performed only by calling methods of this class.
     * Instances of this class cannot be created by calling a constructor: an instance can only be obtained from the argument of the callback passed to method {@link #run(Consumer, Function)}.
     * Methods offered by this class belong to two categories: sensor and motor methods are high level operations, though users can directly send low-level
     * commands to the EV3 brick by calling {@link #getSiValue(byte, int, int, int)} and {@link #getPercentValue(byte, int, int, int)} methods.
     *
     * @see #getSiValue(byte, int, int, int)
     * @see #getPercentValue(byte, int, int, int)
     */
    public static class Api implements AutoCloseable {
        /**
         * Public field pointing to the EV3 object.
         */
        @NonNull
        public final EV3 ev3;

        protected Api(@NonNull EV3 ev3) {
            this.ev3 = ev3;
        }

        // high level API
        //

        /**
         * Access the light sensor of EV3.
         *
         * @param port the input port where the light sensor is connected to on the brick.
         * @return an object of type LightSensor.
         */
        @NonNull
        public LightSensor getLightSensor(@NonNull InputPort port) {
            return new LightSensor(this, port);
        }

        /**
         * Access the touch sensor of EV3.
         *
         * @param port the input port where the touch sensor is connected to on the brick.
         * @return an object of type TouchSensor.
         */
        @NonNull
        public TouchSensor getTouchSensor(@NonNull InputPort port) {
            return new TouchSensor(this, port);
        }

        /**
         * Access the ultrasonic sensor of EV3.
         *
         * @param port the input port where the ultrasonic sensor is connected to on the brick.
         * @return an object of type UltrasonicSensor.
         */
        @NonNull
        public UltrasonicSensor getUltrasonicSensor(@NonNull InputPort port) {
            return new UltrasonicSensor(this, port);
        }

        /**
         * Access the gyroscope sensor of EV3.
         *
         * @param port the input port where the gyroscope sensor is connected to on the brick.
         * @return an object of type GyroSensor.
         */
        @NonNull
        public GyroSensor getGyroSensor(@NonNull InputPort port) {
            return new GyroSensor(this, port);
        }

        /**
         * Access the tacho motor of EV3.
         *
         * @param port the output port where the motor is connected to on the brick.
         * @return an object of type TachoMotor.
         */
        @NonNull
        public TachoMotor getTachoMotor(@NonNull OutputPort port) {
            return new TachoMotor(this, port);
        }

        /**
         * Play a sound tone on the EV3 brick.
         *
         * @param volume   volume within the range [0 - 100].
         * @param freq     frequency in the range [ 250 - 10000 ].
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

        // mid level API
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
         * This method sends the opInput_Device command READY_SI according to the official EV3 Developer Kit Documentation.
         *
         * @param port   port number.
         * @param type   type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode   mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 32-bit floats whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms EV3 Firmware Developer Kit</a>
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
         * This method sends the opInput_Device command READY_PCT according to the official EV3 Developer Kit Documentation.
         *
         * @param port   port number.
         * @param type   type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode   mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 16-bit integers whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms EV3 Firmware Developer Kit</a>
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
         *
         * @param c   functional object of type {@link Callable}.
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
         *
         * @param reservation global reservation for the result in bytes.
         * @param bc          object of type {@link Bytecode} representing the command to be sent.
         * @return a {@link Future} object hosting the {@link Reply} object wrapping the reply by EV3.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms EV3 Firmware Developer Kit</a>
         */
        @NonNull
        public Future<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException {
            return ev3.channel.send(reservation, bc);
        }

        /**
         * Low level send command with no reply.
         *
         * @param bc object of type {@link Bytecode} representing the command to be sent.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms EV3 Firmware Developer Kit</a>
         */
        public void sendNoReply(@NonNull Bytecode bc) throws IOException {
            ev3.channel.sendNoReply(bc);
        }

        @SuppressWarnings("RedundantThrows")
        @Override
        public void close() throws Exception {
        }
    }
}
