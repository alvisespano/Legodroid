package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

public class LightSensor extends AbstractSensor {
    public LightSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port);
    }

    public Future<Integer> getReflected() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_REFLECT, 1);
        return api.execAsync(() -> (int) f.get()[0]);
    }

    public Future<Integer> getAmbient() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_AMBIENT, 1);
        return api.execAsync(() -> (int) f.get()[0]);
    }

    public Future<Color> getColor() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_COLOR, 1);
        return api.execAsync(() -> Color.values()[f.get()[0]]);
    }

    public static class Rgb {
        public final int R, G, B;

        public Rgb(int R, int G, int B) {
            this.R = R;
            this.G = G;
            this.B = B;
        }
    }

    public Future<Rgb> getRgb() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_RGB, 3);
        return api.execAsync(() -> {
                short[] rgb = f.get();
                return new Rgb(rgb[0], rgb[1], rgb[2]);
        });
    }

    public enum Color {
        TRANSPARENT (0),
        BLACK (1),
        BLUE (2),
        GREEN (3),
        YELLOW (4),
        RED (5),
        WHITE (6),
        BROWN (7);

        private final int val;

        Color(int n) {
            val = n;
        }
    }

}
