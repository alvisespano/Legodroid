package it.unive.dais.legodroid.lib.comm;

import java.io.IOException;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;

/**
 * Classes implementing this interface allow connecting to some devices.
 * Connection produces a subtype of Channel object, as a handler representing an active connection through the object life cycle.
 *
 * @param <C> generic type extending {@link Channel} that is returned by method {@link #call()}
 */
public interface Connection<P, C extends Channel<P>> extends Callable<C> {
    /**
     * Produces a channel object that allows communication by sending commands and receiving replies.
     *
     * @return an object of type {@link C}, subtype of {@link Channel}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    C call() throws IOException;
    @NonNull
    P getPeer();
}
