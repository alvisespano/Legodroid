package it.unive.dais.legodroid.lib;

import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;

public class Api {
    private EV3 ev3;

    Api(EV3 ev3) {
        this.ev3 = ev3;
    }

    public LightSensor getLightSensor(int port) {
        return new LightSensor(ev3, port);
    }

    public TouchSensor getTouchSensor(int port) {
        return new TouchSensor(ev3, port);
    }
}
