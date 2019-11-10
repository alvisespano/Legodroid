package it.unive.dais.legodroid.lib.plugs;



import androidx.annotation.NonNull;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Instances of this class allow operations on the touch (or pressure) sensor connected to GenEV3 via an input port.
 */
public class TouchSensor extends AbstractSensor {
    public TouchSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_TOUCH);
    }

    /**
     * Get the pressed status of the touch sensor.
     *
     * @return true when the sensor is pressed; false otherwise.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    public Future<Boolean> getPressed() throws IOException {
        return getPercent1(Const.TOUCH_TOUCH, (x) -> x > 0);
    }
}
