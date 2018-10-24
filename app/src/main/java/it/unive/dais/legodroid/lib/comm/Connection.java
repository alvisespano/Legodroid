package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;

public interface Connection extends AutoCloseable {
    Channel connect() throws IOException;
    void disconnect() throws IOException;
}
