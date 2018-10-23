package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public abstract class AbstractSensor<TMode> {
    protected int port;
    protected TMode currentMode;
    protected EV3 ev3;

    protected AbstractSensor(EV3 ev3, int port, TMode startMode) {
        this.port = port;
        this.currentMode = startMode;
        this.ev3 = ev3;
    }
}
