package it.unive.dais.legodroid.lib.plugs;

import java.io.IOException;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

/**
 * Instances of this class allow operations on the light sensor connected to GenEV3 via an input port.
 */
public class LightSensor extends AbstractSensor {
    public LightSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_COLOR);
    }

    /**
     * Get the reflected light from the sensor (device mode GenEV3-Color-Reflected).
     * Sets the sensor LED color to red.
     *
     * @return a {@link Future} object hosting the 16-bit integer within the range [ 0 - 100 ] returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public Future<Short> getReflected() throws IOException {
        return getPercent1(Const.COL_REFLECT);
    }

    /**
     * Get the ambient light from the sensor (device mode GenEV3-Color-Ambient).
     * Sets the sensor LED color to blue (dimly lit).
     *
     * @return a {@link Future} object hosting the 16-bit integer within the range [ 0 - 100 ] returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public Future<Short> getAmbient() throws IOException {
        return getPercent1(Const.COL_AMBIENT);
    }

    /**
     * Get the color value from the sensor (device mode GenEV3-Color-Color).
     * Sets the sensor LED color to white (all LEDs rapidly cycling).
     *
     * @return a {@link Future} object hosting the value of type {@link Color} returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public Future<Color> getColor() throws IOException {
        return getSi1(Const.COL_COLOR, (x) -> Color.values()[(int) (float) x]);
    }

    /**
     * Get the raw RGB values from the sensor (device mode GenEV3-Color-RGB-Raw).
     * Sets the sensor LED color to white (all LEDs rapidly cycling).
     *
     * @return a {@link Future} object hosting the object of type {@link Rgb} returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     * @deprecated On current GenEV3 firmwares this command seems to return wrong or imprecise values. Use at own risk.
     */
    @Deprecated
    @NonNull
    public Future<Rgb> getRgb() throws IOException {
        return getSi(Const.COL_RGB, 3, (rgb) -> new Rgb((int) rgb[0], (int) rgb[1], (int) rgb[2]));
    }

    /**
     * This class represents a raw RGB color value via a triple of integers.
     */
    public static class Rgb {
        public final int R, G, B;

        /**
         * Create an object given the 3 integer values for each color component.
         *
         * @param R 8-bit red component in range [ 0 - 255 ]
         * @param G 8-bit red component in range [ 0 - 255 ]
         * @param B 8-bit red component in range [ 0 - 255 ]
         */
        public Rgb(int R, int G, int B) {
            this.R = R;
            this.G = G;
            this.B = B;
        }

        /**
         * Calculate the RGB 24-bit color value (8 bits for each component).
         *
         * @return the RGB24 value as an integer.
         */
        public int toRGB24() {
            return R << 16 | G << 8 | B;
        }

        /**
         * Calculate the ARGB 32-bit color value (four 8-bit components, including alpha channel).
         * Alpha channel defaults to 255 for maximum opaqueness.
         *
         * @return the ARGB32 value as an integer.
         */
        public int toARGB32() {
            return 0xff000000 | toRGB24();
        }

    }

    /**
     * This enum type represents the possible colors returned by the sensor in device mode GenEV3-Color-RGB-Raw.
     */
    public enum Color {
        TRANSPARENT,
        BLACK,
        BLUE,
        GREEN,
        YELLOW,
        RED,
        WHITE,
        BROWN;

        /**
         * Calculate the ARGB 32-bit color value (8 bits for each component, including alpha channel).
         *
         * @return the ARGB32 value as an integer.
         */
        public int toARGB32() {
            switch (this) {
                case TRANSPARENT:
                    return 0x00000000;
                case BLACK:
                    return 0xff000000;
                case BLUE:
                    return 0xff0000ff;
                case GREEN:
                    return 0xff00ff00;
                case YELLOW:
                    return 0xffffff00;
                case RED:
                    return 0xffff0000;
                case WHITE:
                    return 0xffffffff;
                default:
                    return 0xff000000 | 180 << 16 | 142 << 8 | 92;
            }
        }

    }
}
