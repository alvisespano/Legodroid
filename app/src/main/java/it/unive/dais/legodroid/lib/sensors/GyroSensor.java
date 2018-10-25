package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public class GyroSensor extends AbstractSensor {
    public GyroSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public void calibrate() {

    }

    public int getAngle() {
        return 0;
    }

    public int getRotSpeed() {
        return 0;
    }
}
