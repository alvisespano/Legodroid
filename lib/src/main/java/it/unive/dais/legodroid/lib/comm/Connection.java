package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Classes implementing this interface allow connecting to some devices.
 * Connection produces a subtype of Channel object, as a handler representing an active connection through the object life cycle.
 *
 * @param <C> generic type extending {@link Channel} that is returned by method {@link #connect()}
 */
public interface Connection<C extends Channel> {
    /**
     * Factory method for producing a channel object that allows communicating by sending commands and receiving replies.
     *
     * @return an object of type {@link C}, subtype of {@link Channel}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    C connect() throws IOException;
}
