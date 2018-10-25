package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Channel {
    void write(Packet data) throws IOException;
    Packet read() throws IOException, TimeoutException;
}
