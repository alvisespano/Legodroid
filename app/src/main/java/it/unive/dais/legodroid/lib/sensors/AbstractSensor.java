package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public abstract class AbstractSensor {
    protected EV3.InputPort port;
    protected EV3.Api api;

    protected AbstractSensor(EV3.Api api, EV3.InputPort port) {
        this.port = port;
        this.api = api;
    }
}
