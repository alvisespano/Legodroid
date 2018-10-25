package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Channel<Input, Output> {
    void write(Input data) throws IOException;
    Output read() throws IOException, TimeoutException;
}
