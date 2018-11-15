package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;

public interface Channel extends AutoCloseable {
    void write(@NonNull Command data) throws IOException;
    @NonNull
    Reply read() throws IOException;
}
