package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

import java.io.IOException;

public class TachoMotor {
    private EV3.Api api;
    private EV3.OutputPort port;

    public TachoMotor(EV3.Api api, EV3.OutputPort port) {
        this.api = api;
        this.port = port;
    }

    public int getPosition() {
        return 0;
    }

    public void resetPosition() {

    }

    public boolean isStalled() {
        return false;
    }

    public void goToPositionRel(int amount) {

    }

    public void goToPositionAbs(int pos) {

    }

    public void forward(int speed) throws IOException {
        api.setOutputState(port.toByte(), speed);
    }

    public void backward(int speed) {

    }

    public void brake() {

    }

    public void stop() {

    }

    public boolean isMoving() { // TODO: questo non è un doppione con isStill()?
        return false;
    }
}
