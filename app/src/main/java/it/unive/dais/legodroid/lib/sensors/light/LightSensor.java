package it.unive.dais.legodroid.lib.sensors.light;

import it.unive.dais.legodroid.lib.sensors.BaseSensor;

public class LightSensor extends BaseSensor<LightSensorModes> {
    public LightSensor(int port) {
        super(port, LightSensorModes.REFLECTIVE);
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
