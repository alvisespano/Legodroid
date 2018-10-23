package it.unive.dais.legodroid.lib.lowlevel;

import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;

public class PacketManager {
    private Connector connector;
    private int sequenceCounter;

    public PacketManager(Connector connector) {
        this.connector = connector;
        this.sequenceCounter = 0;
    }

    void sendPacketAsyncNoReply(final byte[] bytecode, final int localReservation,
                                final int globalReservation) throws IOException {
        DirectCommandPacket packet = new DirectCommandPacket(sequenceCounter, false, localReservation, globalReservation,
                bytecode);
        connector.write(packet.getBytes());
    }

    Promise<DirectCommandReply> sendPacketAsyncReply(final byte[] bytecode, final int localReservation,
                                                     final int globalReservation) throws IOException {
        DirectCommandPacket packet = new DirectCommandPacket(sequenceCounter, true, localReservation, globalReservation,
                bytecode);
        connector.write(packet.getBytes());

        final Promise<DirectCommandReply> promise = new Promise<>();

        connector.read(2).then(new Consumer<byte[]>() {
            @Override
            public void call(byte[] data) {
                final int length = data[0] << 8 & 0xFF00 | data[1] & 0xFF;
                final byte[] lengthHeader = data;
                try {
                    connector.read(length).then(new Consumer<byte[]>() {
                        @Override
                        public void call(byte[] data) {
                            byte[] resultBytes = new byte[length + 2];
                            System.arraycopy(lengthHeader, 0, resultBytes, 0, 2);
                            System.arraycopy(data, 0, resultBytes, 2, length);
                            DirectCommandReply reply = DirectCommandReply.fromBytes(resultBytes);
                            promise.resolve(reply);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sequenceCounter++;
        return promise;
    }
}
