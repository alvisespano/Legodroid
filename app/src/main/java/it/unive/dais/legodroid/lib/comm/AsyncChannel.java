package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

public interface AsyncChannel extends AutoCloseable {
    @NonNull
    Future<Reply> send(@NonNull Command cmd) throws IOException;
    @NonNull
    Future<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException;
    void sendNoReply(@NonNull Bytecode bc) throws IOException;
}
