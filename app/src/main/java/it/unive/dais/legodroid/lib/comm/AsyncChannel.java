package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public interface AsyncChannel extends Channel<Packet, Future<Packet>> {
    void write(Packet data) throws IOException;
    Future<Packet> read() throws IOException, TimeoutException;
}
