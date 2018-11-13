package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import it.unive.dais.legodroid.lib.EV3;

public abstract class Plug<Port> {
    @NonNull
    protected final EV3.Api api;
    @NonNull
    protected final Port port;

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
