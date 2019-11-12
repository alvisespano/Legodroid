package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Instances of this class allow operations on the ultrasonic sensor connected to GenEV3 via an input port.
 */
public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_ULTRASONIC);
    }

    /**
     * Get the distance status of the touch sensor.
     *
     * @return true when the sensor is pressed; false otherwise.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public Future<Float> getDistance() throws IOException {
        return getSi1(Const.US_CM);
    }
}
