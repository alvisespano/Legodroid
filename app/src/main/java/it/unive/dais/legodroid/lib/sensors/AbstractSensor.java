package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.InputPort;

public abstract class AbstractSensor {
    protected InputPort port;
    protected EV3.Api api;

    protected AbstractSensor(EV3.Api api, InputPort port) {
        this.port = port;
        this.api = api;
    }
}
