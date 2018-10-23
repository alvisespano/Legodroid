package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public abstract class AbstractSensor {
    protected int port;
    protected EV3 ev3;

    protected AbstractSensor(EV3 ev3, int port) {
        this.port = port;
        this.ev3 = ev3;
    }
}
