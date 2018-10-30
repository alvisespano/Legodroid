package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Promise;


public class GyroSensor extends AbstractSensor {
    public GyroSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port);
    }

//    public Promise<Boolean> getPressed() throws IOException {
//        final Promise<Boolean> promise = new Promise<>();  // TODO: da rifare
////        api.getSiValue(port, EV3_TOUCH, TOUCH_TOUCH, 1).then((Consumer<float[]>) values -> promise.resolve((int) values[0] == 1));
//        return promise;
//    }
}
