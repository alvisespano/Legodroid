package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;

public interface Connection extends AutoCloseable {
    @NonNull
    Channel connect() throws IOException;
    void disconnect() throws IOException;
}
