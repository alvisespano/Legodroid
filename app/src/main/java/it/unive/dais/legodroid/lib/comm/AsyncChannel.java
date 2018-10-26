package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public interface AsyncChannel {
    void write(Command data) throws IOException;
    Future<Reply> read() throws IOException, TimeoutException;
}
