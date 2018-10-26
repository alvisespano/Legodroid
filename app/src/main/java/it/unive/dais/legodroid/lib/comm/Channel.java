package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Channel {
    void write(Command data) throws IOException;
    Reply read() throws IOException, TimeoutException;
}
