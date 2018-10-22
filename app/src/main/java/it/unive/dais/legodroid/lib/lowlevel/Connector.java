package it.unive.dais.legodroid.lib.lowlevel;

import java.io.IOException;

public interface Connector {
    void connect() throws Exception;

    void disconnect() throws IOException;

    void write(byte[] data) throws IOException;

    void addDataReceiveListener(DataReceiveListener listener);

    void removeDataReceiveListener(DataReceiveListener listener);
}
