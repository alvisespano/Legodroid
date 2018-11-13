package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

public class LightSensor extends AbstractSensor {
    public LightSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_COLOR);
    }

    @NonNull
    public Future<Short> getReflected() throws IOException {
        return getPercent1(Const.COL_REFLECT);
    }

    @NonNull
    public Future<Short> getAmbient() throws IOException {
        return getPercent1(Const.COL_AMBIENT);
    }

    @NonNull
    public Future<Color> getColor() throws IOException {
        return getPercent1(Const.COL_COLOR, (x) -> Color.values()[x]);

    }

    public static class Rgb {
        public final int R, G, B;

        public Rgb(int R, int G, int B) {
            this.R = R;
            this.G = G;
            this.B = B;
        }
    }

    @NonNull
    public Future<Rgb> getRgb() throws IOException {
        return getPercent(Const.COL_COLOR, 3, (rgb) -> new Rgb(rgb[0], rgb[1], rgb[2]));
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
