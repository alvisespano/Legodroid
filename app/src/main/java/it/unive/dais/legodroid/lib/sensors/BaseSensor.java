package it.unive.dais.legodroid.lib.sensors;

public abstract class BaseSensor<TMode> {
    protected int port;
    protected TMode currentMode;
    protected EV3 ev3;

    protected BaseSensor(EV3 ev3, int port, TMode startMode) {
        this.port = port;
        this.currentMode = startMode;
        this.ev3 = ev3;
    }
}
