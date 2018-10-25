package it.unive.dais.legodroid.lib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Constants;
import it.unive.dais.legodroid.lib.comm.DirectCommandPacket;
import it.unive.dais.legodroid.lib.comm.DirectCommandReply;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.GyroSensor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.sensors.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Promise;

public class EV3 {
    private AsyncChannel channel;
    private int sequenceCounter = 0;
    private Consumer<Event> eventListener;
    private Api api;

    public interface Event {}

    public EV3(AsyncChannel channel) {
        this.channel = channel;
    }

    public void sendPacketAsyncNoReply(byte[] bytecode, int localReservation, int globalReservation) throws IOException {
        DirectCommandPacket packet = new DirectCommandPacket(sequenceCounter, false, localReservation, globalReservation, bytecode);
        channel.write(packet);
    }

    public Promise<DirectCommandReply> sendPacketAsyncReply(byte[] bytecode, int localReservation, int globalReservation) throws IOException {
        DirectCommandPacket packet = new DirectCommandPacket(sequenceCounter, true, localReservation, globalReservation, bytecode);
        channel.write(packet);

        Promise<DirectCommandReply> promise = new Promise<>();

//        channel.read(2).onSuccess(data -> {
//            int length = data[0] << 8 & 0xFF00 | data[1] & 0xFF;
//            byte[] lengthHeader = data;
//            try {
//                conn.read(length).onSuccess(data1 -> {      // TODO: questo è un caso d'uso di doppio onSuccess() nella Promise: possiamo inventarci qualcosa di meglio?
//                    byte[] resultBytes = new byte[length + 2];
//                    System.arraycopy(lengthHeader, 0, resultBytes, 0, 2);
//                    System.arraycopy(data1, 0, resultBytes, 2, length);
//                    DirectCommandReply reply = DirectCommandReply.fromBytes(resultBytes);
//                    promise.resolve(reply);
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        sequenceCounter++;
        return promise;
    }

    public void run(Consumer<Api> c) {
        c.call(api);
    }

    public void sendEvent(Event e) {
        api.incomingEvents.add(e);
    }

    public void setEventListener(Consumer<Event> eventListener) {
        this.eventListener = eventListener;
    }

    public class Api {
        private EV3 ev3;
        private Queue<Event> incomingEvents;

        Api(EV3 ev3) {
            this.ev3 = ev3;
            this.incomingEvents = new ConcurrentLinkedQueue<>();
        }

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

//        public void playSoundTone(int volume, int freq, int duration) throws IOException {
//            ev3.soundTone(volume, freq, duration);
//        }

        public Event pollEvents() {
            return incomingEvents.poll();
        }

        public void sendEvent(Event e) {
            eventListener.call(e);
        }
    }

    public class Comm {
        private static final byte OUTPUT_PORT_OFFSET = 0x10;

        private Bytecode preface(byte ready, int port, int type, int mode, int nvalue) throws IOException {
            Bytecode r = new Bytecode();
            r.addOpCode(Constants.INPUT_DEVICE);
            r.addOpCode(ready);
            r.addParameter(Constants.LAYER_MASTER);
            r.addParameter((byte) port);
            r.addParameter((byte) type);
            r.addParameter((byte) mode);
            r.addParameter((byte) nvalue);
            r.addGlobalIndex((byte) 0x00);
            return r;
        }

        public Promise<float[]> getSiValue(int port, int type, int mode, int nvalue) throws IOException {
            //        Bytecode byteCode = new Bytecode();
            //        byteCode.addOpCode(Constants.INPUT_DEVICE);
            //        byteCode.addOpCode(Constants.READY_SI);
            //        byteCode.addParameter(Constants.LAYER_MASTER);
            //        byteCode.addParameter((byte) port);
            //        byteCode.addParameter((byte) type);
            //        byteCode.addParameter((byte) mode);
            //        byteCode.addParameter((byte) nvalue);
            //        byteCode.addGlobalIndex((byte) 0x00);
            Bytecode byteCode = preface(Constants.READY_SI, port, type, mode, nvalue);

            final Promise<float[]> returnPromise = new Promise<>();
            // TODO: è tutto overcomplicated: bisogna semplificare sto casino

            Promise<DirectCommandReply> replyPromise = sendPacketAsyncReply(byteCode.getBytes(), 0, 4 * nvalue);
            replyPromise.onSuccess(data -> {
                byte[] reply = data.getBytes();
                float[] result = new float[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    byte[] bData = Arrays.copyOfRange(reply, 3 + 4 * i, 7 + 4 * i);
                    result[i] = ByteBuffer.wrap(bData).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                }

                returnPromise.resolve(result);
            });

            return returnPromise;
        }

        public Promise<short[]> getPercentValue(int port, int type, int mode, int nvalue) throws IOException {
            //        Bytecode byteCode = new Bytecode();
            //        byteCode.addOpCode(Constants.INPUT_DEVICE);
            //        byteCode.addOpCode(Constants.READY_PCT);
            //        byteCode.addParameter(Constants.LAYER_MASTER);
            //        byteCode.addParameter((byte) port);
            //        byteCode.addParameter((byte) type);
            //        byteCode.addParameter((byte) mode);
            //        byteCode.addParameter((byte) nvalue);
            //        byteCode.addGlobalIndex((byte) 0x00);
            Bytecode byteCode = preface(Constants.READY_PCT, port, type, mode, nvalue);

            final Promise<short[]> returnPromise = new Promise<>();

            Promise<DirectCommandReply> replyPromise = sendPacketAsyncReply(byteCode.getBytes(), 0, nvalue);
            replyPromise.onSuccess(data -> {
                byte[] reply = data.getBytes();
                short[] result = new short[nvalue];
                for (int i = 0; i < nvalue; i++) {
                    result[i] = (short) reply[3 + i];
                }

                returnPromise.resolve(result);
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


        // TODO: queste forse non servono; o comunque vanno risistemate prima o poi
        public void setOutputState(int port, int speed) throws IOException {
            Bytecode byteCode = new Bytecode();

            byte byteCodePort = toByteCodePort(port);

            byteCode.addOpCode(Constants.OUTPUT_POWER);
            byteCode.addParameter(Constants.LAYER_MASTER);
            byteCode.addParameter(byteCodePort);
            byteCode.addParameter((byte) speed);

            byteCode.addOpCode(Constants.OUTPUT_START);
            byteCode.addParameter(Constants.LAYER_MASTER);
            byteCode.addParameter(byteCodePort);

            sendPacketAsyncNoReply(byteCode.getBytes(), 0, 0);
        }

        public void soundTone(int volume, int freq, int duration) throws IOException {
            Bytecode byteCode = new Bytecode();

            byteCode.addOpCode(Constants.SOUND_CONTROL);
            byteCode.addOpCode(Constants.SOUND_TONE);
            byteCode.addParameter((byte) volume);
            byteCode.addParameter((short) freq);
            byteCode.addParameter((short) duration);

            sendPacketAsyncNoReply(byteCode.getBytes(), 0, 0);
        }
    }
}
