package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    float getDistance() {
        return 0.0f;
    }
}
