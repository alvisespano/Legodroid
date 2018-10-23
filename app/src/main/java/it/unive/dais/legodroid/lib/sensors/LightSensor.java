package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;

public class LightSensor extends AbstractSensor {
    public LightSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public int getReflected() {
        return 0;
    }

    public int getAmbient() {
        return 0;
    }

    public Color getColor() {
        return Color.BLACK;
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

    public enum Color {
        TRANSPARENT,
        BLACK,
        BLUE,
        GREEN,
        YELLOW,
        RED,
        WHITE,
        BROWN
    }

}
