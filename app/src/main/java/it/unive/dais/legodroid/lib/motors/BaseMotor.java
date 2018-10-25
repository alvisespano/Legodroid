package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public class BaseMotor implements Motor {
    protected EV3 ev3;
    protected int port;

    BaseMotor(EV3 ev3, int port) {
        this.ev3 = ev3;
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void forward(int speed) {

    }

    @Override
    public void backward(int speed) {

    }

    @Override
    public void brake() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isMoving() {
        return false;
    }
}
