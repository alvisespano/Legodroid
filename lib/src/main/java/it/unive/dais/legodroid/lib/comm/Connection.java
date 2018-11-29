package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Classes implementing this interface allow connecting to some devices.
 * Connection produces a subtype of Channel object, as a handler representing an active connection through the object life cycle.
 */
public interface Connection<C extends Channel> {
    /**
     * Factory method for producing a channel object that allows communicating by sending commands and receiving replies.
     * @return an object of type {@code C}, subtype of {@link Channel}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    C connect() throws IOException;
}
