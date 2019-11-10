package it.unive.dais.legodroid.lib.comm;



import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Classes implementing this interface represent active on-going connections between two devices, in a similar fashion to {@link Channel}.
 * Communication is <b>asynchronous</b> though: no receive method exists, as calling {@link #send(Command)} returns a future reply.
 */
public interface AsyncChannel extends AutoCloseable {
    /**
     * Send a {@link Command} asynchronously and returns a future object hosting the {@link Reply} object.
     *
     * @param cmd the command to be sent.
     * @return the future object hosting the reply.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    Future<Reply> send(@NonNull Command cmd) throws IOException;

    /**
     * Send a {@link Command} asynchronously and returns a future object hosting the {@link Reply} object.
     * Can specify the global reservation for the command on the GenEV3 side.
     *
     * @param reservation number of bytes for the global reservation on the GenEV3.
     * @param bc          object of type Bytecode with the command.
     * @return the future object hossting the reply.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    Future<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException;

    /**
     * Lower-level method for sending a custom-built Bytecode objects as commands.
     *
     * @param bc the object of type Bytecode.
     * @throws IOException thrown when communication errors occur.
     */
    void sendNoReply(@NonNull Bytecode bc) throws IOException;

    /**
     * Schedule disconnection from the device.
     * Calling this method explicitly invalidates the object, therefore it is recommended to trigger the {@link AutoCloseable} behaviour by losing the reference to the object instead, when you need to disconnect.
     */
    @Override
    void close();
}
