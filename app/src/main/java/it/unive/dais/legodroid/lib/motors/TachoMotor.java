package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public class TachoMotor extends AbstractMotor {
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

    // TODO: implementare questi con i nomi dell'interfaccia, non come i metodi sopra che hanno nomi diversi; oppure cambiare l'interfaccia. Ma insomma: bisogna fare ordine

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
