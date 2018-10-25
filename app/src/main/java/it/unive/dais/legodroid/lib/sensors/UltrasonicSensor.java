package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;

import static it.unive.dais.legodroid.lib.comm.Constants.EV3_TOUCH;
import static it.unive.dais.legodroid.lib.comm.Constants.TOUCH_TOUCH;


public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public Promise<Boolean> getPressed() throws IOException {
        final Promise<Boolean> promise = new Promise<>();  // TODO: da rifare
//        ev3.getSiValue(port, EV3_TOUCH, TOUCH_TOUCH, 1).then((Consumer<float[]>) values -> promise.resolve((int) values[0] == 1));
        return promise;
    }
}
