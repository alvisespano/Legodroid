package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

public class LightSensor extends AbstractSensor {
    public LightSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port);
    }

    public FutureTask<Integer> getReflected() throws IOException {
        FutureTask<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_REFLECT, 1);
        return new FutureTask<>(() -> (int) f.get()[0]);
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
