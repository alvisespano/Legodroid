package it.unive.dais.legodroid.lib.lowlevel;

import java.io.IOException;

public interface Connector {
    void connect() throws Exception;

    void disconnect() throws IOException;

    void write(byte[] data) throws IOException;

    Promise<byte[]> read(int size) throws IOException;
}
