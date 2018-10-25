package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public class TachoMotor extends BaseMotor {
    public TachoMotor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public int getPosition() {
        return 0;
    }

    public void resetPosition() {

    }

    public boolean isStill() {
        return false;
    }

    public void goToPositionRel(int amount) {

    }

    public void goToPositionAbs(int pos) {

    }
}
