package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import it.unive.dais.legodroid.lib.util.Promise;

public interface AsyncChannel {
    void write(Packet data) throws IOException;
    Promise<Packet> read() throws IOException, TimeoutException;
}
