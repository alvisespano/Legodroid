package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public class TachoMotor extends AbstractMotor {
    public TachoMotor(EV3.Api api, EV3.OutputPort port) {
        super(api, port);
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
    public boolean isMoving() { // TODO: questo non è un doppione con isStill()?
        return false;
    }
}
