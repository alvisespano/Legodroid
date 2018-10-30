package it.unive.dais.legodroid.lib.motors;

import android.support.annotation.NonNull;

import it.unive.dais.legodroid.lib.EV3;

public abstract class AbstractMotor implements Motor {
    protected EV3.Api api;
    protected EV3.OutputPort port;

    AbstractMotor(@NonNull EV3.Api api, EV3.OutputPort port) {
        this.api = api;
        this.port = port;
    }

    @Override
    public EV3.OutputPort getPort() {
        return port;
    }
}
