package it.unive.dais.legodroid.lib.lowlevel;

import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;

public interface Connector {
    void connect() throws Exception;

    void disconnect() throws IOException;

    Promise<Void> write(byte[] data) throws IOException;

    Promise<byte[]> read(int size) throws IOException;
}
