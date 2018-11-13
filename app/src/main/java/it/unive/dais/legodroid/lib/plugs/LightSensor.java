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
        return getSi1(Const.COL_COLOR, (x) -> Color.values()[(int) (float) x]);
    }

    // TODO: this does not work on current EV3
    @Deprecated
    @NonNull
    public Future<Rgb> getRgb() throws IOException {
        return getSi(Const.COL_RGB, 3, (rgb) -> new Rgb((int) rgb[0], (int) rgb[1], (int) rgb[2]));
    }

    public static class Rgb {
        public final int R, G, B;

        public Rgb(int R, int G, int B) {
            this.R = R;
            this.G = G;
            this.B = B;
        }

        public int toRGB24() {
            return R << 16 | G << 8 | B;
        }

        public int toARGB32() {
            return 0xff000000 | toRGB24();
        }

    }

    public enum Color {
        TRANSPARENT,
        BLACK,
        BLUE,
        GREEN,
        YELLOW,
        RED,
        WHITE,
        BROWN;

        public int toARGB32() {
            switch (this) {
                case TRANSPARENT:   return 0x00000000;
                case BLACK:         return 0xff000000;
                case BLUE:          return 0xff0000ff;
                case GREEN:         return 0xff00ff00;
                case YELLOW:        return 0xffffff00;
                case RED:           return 0xffff0000;
                case WHITE:         return 0xffffffff;
                default:            return 0xff000000 | 180 << 16 | 142 << 8 | 92;
            }
        }

    }
}
