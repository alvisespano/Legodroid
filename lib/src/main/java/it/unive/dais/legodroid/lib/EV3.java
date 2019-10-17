package it.unive.dais.legodroid.lib;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.comm.Reply;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * Specialized EV3 class with default Api.
 * Use this commodity class when you do not need to extend the Api class.
 */
public class EV3 extends GenEV3<EV3.Api> {
    /**
     * Facility constructor. Creates a SpooledAsyncChannel automatically with the given synchrounous channel
     *
     * @param ch a synchrounous channel object.
     */
    public EV3(@NonNull AsyncChannel ch) {
        super(ch);
    }

    /**
     * Facility constructor. Creates a SpooledAsyncChannel automatically with the given synchrounous channel
     *
     * @param ch a synchrounous channel object.
     */
    public EV3(@NonNull Channel ch) {
        super(ch);
    }

    /**
     * Run the given callback as the main program for the EV3 brick.
     *
     * @param f the function object representing the main code for the EV3 brick.
     * @throws AlreadyRunningException thrown when a program is already running on the EV3 brick.
     */
    public void run(@NonNull Consumer<Api> f) throws AlreadyRunningException {
        run(f, Api::new);

        try (Api api = new Api(this)) {

        } catch (Exception e) {
            e.printStackTrace();
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
         *
         * @return a byte according to the encoding defined by the GenEV3 Developer Kit Documentation.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
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
         * Encode the output port as a bit mask for certain GenEV3 direct commands that require the bitmask format as parameter.
         *
         * @return a byte with the bit mask.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">GenEV3 Developer Kit Documentation</a>
         */
        public byte toBitmask() {
            return (byte) (1 << toByte());
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         * Using output ports for receive operations is possible, though a special encoding is needed according to the GenEV3 Developer Kit Documentation - this is provided by this method.
         *
         * @return a byte according to the encoding defined by the GenEV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">GenEV3 Developer Kit Documentation</a>
         */
        public byte toByteAsRead() {
            return (byte) (toByte() | 0x10);
        }

        /**
         * Encode the output port into a byte for use with {@link Api#getPercentValue(byte, int, int, int)} and {@link Api#getSiValue(byte, int, int, int)}.
         *
         * @return a byte according to the encoding defined by the GenEV3 Developer Kit Documentation.
         * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">GenEV3 Developer Kit Documentation</a>
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
     * This inner class contains the operations that can be performed to control the GenEV3 brick.
     * Accesing sensors, moving motors etc. are operations that can be performed only by calling methods of this class.
     * Instances of this class cannot be created by calling a constructor: an instance can only be obtained from the argument of the callback passed to method {@link #run(Consumer, Function)}.
     * Methods offered by this class belong to two categories: sensor and motor methods are high level operations, though users can directly send low-level
     * commands to the GenEV3 brick by calling {@link #getSiValue(byte, int, int, int)} and {@link #getPercentValue(byte, int, int, int)} methods.
     *
     * @see #getSiValue(byte, int, int, int)
     * @see #getPercentValue(byte, int, int, int)
     */
    public static class Api implements AutoCloseable {
        private static final String TAG = ReTAG("Api");

        /**
         * Public field pointing to the GenEV3 object.
         */
        @NonNull
        public final GenEV3<? extends Api> ev3;

        protected Api(@NonNull GenEV3<? extends Api> ev3) {
            this.ev3 = ev3;
        }

        // high level API
        //

        /**
         * Access the light sensor of GenEV3.
         *
         * @param port the input port where the light sensor is connected to on the brick.
         * @return an object of type LightSensor.
         */
        @NonNull
        public LightSensor getLightSensor(@NonNull InputPort port) {
            return new LightSensor(this, port);
        }

        /**
         * Access the touch sensor of GenEV3.
         *
         * @param port the input port where the touch sensor is connected to on the brick.
         * @return an object of type TouchSensor.
         */
        @NonNull
        public TouchSensor getTouchSensor(@NonNull InputPort port) {
            return new TouchSensor(this, port);
        }

        /**
         * Access the ultrasonic sensor of GenEV3.
         *
         * @param port the input port where the ultrasonic sensor is connected to on the brick.
         * @return an object of type UltrasonicSensor.
         */
        @NonNull
        public UltrasonicSensor getUltrasonicSensor(@NonNull InputPort port) {
            return new UltrasonicSensor(this, port);
        }

        /**
         * Access the gyroscope sensor of GenEV3.
         *
         * @param port the input port where the gyroscope sensor is connected to on the brick.
         * @return an object of type GyroSensor.
         */
        @NonNull
        public GyroSensor getGyroSensor(@NonNull InputPort port) {
            return new GyroSensor(this, port);
        }

        /**
         * Access the tacho motor of GenEV3.
         *
         * @param port the output port where the motor is connected to on the brick.
         * @return an object of type TachoMotor.
         */
        @NonNull
        public TachoMotor getTachoMotor(@NonNull OutputPort port) {
            return new TachoMotor(this, port);
        }

        /**
         * Play a sound tone on the GenEV3 brick.
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
         * Low level method for sending direct commands to the GenEV3 brick.
         * This method sends the opInput_Device command READY_SI according to the official GenEV3 Developer Kit Documentation.
         *
         * @param port   port number.
         * @param type   type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode   mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 32-bit floats whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
         */
        @NonNull
        public CompletableFuture<float[]> getSiValue(byte port, int type, int mode, int nvalue) throws IOException {
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
         * Low level method for sending direct commands to the GenEV3 brick.
         * This method sends the opInput_Device command READY_PCT according to the official GenEV3 Developer Kit Documentation.
         *
         * @param port   port number.
         * @param type   type constant as defined in {@link Const}, e.g. {@link Const#EV3_TOUCH} or {@link Const#EV3_COLOR}.
         * @param mode   mode constant as defined in {@link Const}, e.g. {@link Const#COL_AMBIENT} or {@link Const#GYRO_ANGLE}.
         * @param nvalue number of values the command expects to return in the result array.
         * @return a future object containing an array of 16-bit integers whose length is equal to parameter {@code nvalues}.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
         */
        @NonNull
        public CompletableFuture<short[]> getPercentValue(byte port, int type, int mode, int nvalue) throws IOException {
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
         * @param <T> the return type of the callback.
         * @param f   functional object of type {@link Callable}.
         * @return a {@link CompletableFuture} object hosting the result of type {@code T}.
         */
        @NonNull
        public <T> CompletableFuture<T> execAsync(@NonNull Callable<T> f) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return f.call();
                } catch (Exception e) {
                    Log.w(TAG, "execAsync(): exception caught when executing callback");
                    throw new RuntimeException(e);
                }
            });
        }

        /**
         * Low level send command with reply.
         *
         * @param reservation global reservation for the result in bytes.
         * @param bc          object of type {@link Bytecode} representing the command to be sent.
         * @return a {@link Future} object hosting the {@link Reply} object wrapping the reply by GenEV3.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
         */
        @NonNull
        public CompletableFuture<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException {
            return ev3.channel.send(reservation, bc);
        }

        /**
         * Low level send command with no reply.
         *
         * @param bc object of type {@link Bytecode} representing the command to be sent.
         * @throws IOException thrown when communication errors occur.
         * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
         */
        public void sendNoReply(@NonNull Bytecode bc) throws IOException {
            ev3.channel.sendNoReply(bc);
        }

        @Override
        public void close() throws Exception {

        }
    }
}
