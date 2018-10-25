package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public abstract class AbstractMotor implements Motor {
    protected EV3 ev3;
    protected int port;

    AbstractMotor(EV3 ev3, int port) {
        this.ev3 = ev3;
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }
}
