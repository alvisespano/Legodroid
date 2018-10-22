package it.unive.dais.legodroid.lib.lowlevel;

import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;
import java.util.*;

public class PacketManager implements DataReceiveListener {
    private Connector connector;
    private int sequenceCounter;
    private List<Promise<DirectCommandReply>> handlers;

    public PacketManager(Connector connector) {
        this.connector = connector;
        this.sequenceCounter = 0;
        this.handlers = new LinkedList<>();

        this.connector.addDataReceiveListener(this);
    }

    public Promise<DirectCommandReply> sendPacketAsync(final byte[] bytecode, final int localReservation,
                                   final int globalReservation) throws IOException {
        DirectCommandPacket packet =
                new DirectCommandPacket(sequenceCounter, localReservation, globalReservation, bytecode);
        connector.write(packet.getBytes());

        Promise<DirectCommandReply> promise = new Promise<>(sequenceCounter);
        handlers.add(promise);
        sequenceCounter++;
        return promise;
    }

    @Override
    public void onDataReceive(byte[] data) {
        DirectCommandReply reply = DirectCommandReply.fromBytes(data);
        for (Promise<DirectCommandReply> handler : this.handlers) {
            if (handler.sequenceCounter == reply.getCounter()) {
                handler.resolve(reply);
                this.handlers.remove(handler);
            }
        }
    }
}
