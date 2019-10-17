package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

/**
 * Instances of this class allow operations on the gyroscope sensor (accelerometer) connected to GenEV3 via an input port.
 */
public class GyroSensor extends AbstractSensor {
    public GyroSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_GYRO);
    }

    /**
     * Get the angle from the sensor in degrees.
     *
     * @return a {@link Future} object hosting the 32-bit float within the range [ -18000 - 18000 ] returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public CompletableFuture<Float> getAngle() throws IOException {
        return getSi1(Const.GYRO_ANGLE);
    }

    /**
     * Get the rate (or rotational speed) from the sensor in degrees per second.
     *
     * @return a {@link Future} object hosting the 32-bit float returned by GenEV3.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public CompletableFuture<Float> getRate() throws IOException {
        return getSi1(Const.GYRO_RATE);
    }
}
