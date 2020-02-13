package it.unive.dais.legodroid.lib.plugs;

import androidx.annotation.NonNull;
import it.unive.dais.legodroid.lib.EV3;

/**
 * This abstract class is a base class for both input and output ports.
 *
 * The full documentation of EV3 commands, with a detailed description of their behaviour, can be found here:
 * https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf
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
     * @param api  the {@link EV3.Api} object.
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
