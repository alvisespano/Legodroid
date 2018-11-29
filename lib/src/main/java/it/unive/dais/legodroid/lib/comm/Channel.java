package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Classes implementing this interface represent active on-going connections between two devices.
 * A channel object offers a higher level API than {@link java.net.Socket}, dealing with structured command and reply types rather than raw bytes.
 * Communication is synchronous and users must be aware of the communication dynamics by calling {@link #send(Command)} and {@link #receive()} accordingly.
 * Channels are also {@link AutoCloseable}, therefore losing the reference to the channel object triggers automatic disconnection.
 */
public interface Channel extends AutoCloseable {
    /**
     * Send a {@link Command} synchronously.
     * @param data the command to be sent.
     * @throws IOException thrown when communication errors occur.
     */
    void send(@NonNull Command data) throws IOException;

    /**
     * Receive a {@link Reply} synchronously, i.e. this method is <b>blocks</b> until a reply is actually read from the channel.
     * @return the object of type {@link Reply}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    Reply receive() throws IOException;

    /**
     * Disconnect from the device.
     * Calling this method explicitly invalidates the object, therefore it is recommended to trigger the {@link AutoCloseable} behaviour by losing the reference to the object instead, when you need to disconnect.
     * @throws IOException thrown when communication errors occur.
     */
    @Override
    void close() throws Exception;
}
