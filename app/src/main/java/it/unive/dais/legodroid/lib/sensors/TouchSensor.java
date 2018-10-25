package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public class TouchSensor extends AbstractSensor {
    public TouchSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public boolean getPressed() {
        return false;
    }
}
