package it.unive.dais.legodroid.lib.sensors.light;

import it.unive.dais.legodroid.lib.sensors.BaseSensor;

public class LightSensor extends BaseSensor<LightSensorModes> {
    public LightSensor(EV3 ev3, int port) {
        super(ev3, port, LightSensorModes.REFLECTIVE);
    }

    public int getReflected() {
        return 0;
    }

    public int getAmbient() {
        return 0;
    }

    public LightSensorColors getColor() {
        return LightSensorColors.BLACK;
    }

    public int getRawR() {
        return 0;
    }

    public int getRawG() {
        return 0;
    }

    public int getRawB() {
        return 0;
    }
}
