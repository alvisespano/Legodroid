package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import it.unive.dais.legodroid.lib.EV3;

/**
 * This abstract class is a base class for both input and output ports.
 *
 * @param <Port> generic type for the {@link #port} field.
 */
public abstract class Plug<Port> {
    @NonNull
    protected final EV3.Api api;
    @NonNull
    protected final Port port;

    /**
     * Constructor.
     *
     * @param api  the {@link it.unive.dais.legodroid.lib.EV3.Api} object.
     * @param port the port of type {@link Port}.
     */
    protected Plug(@NonNull EV3.Api api, @NonNull Port port) {
        this.api = api;
        this.port = port;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), port);
    }
}
