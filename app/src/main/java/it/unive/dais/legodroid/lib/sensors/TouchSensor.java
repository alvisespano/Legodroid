package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.lowlevel.Protocol;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;
import static it.unive.dais.legodroid.lib.lowlevel.Constants.EV3_TOUCH;
import static it.unive.dais.legodroid.lib.lowlevel.Constants.TOUCH_TOUCH;


public class TouchSensor extends AbstractSensor {
    TouchSensor(EV3 ev3, int port) {
        super(ev3, port);
    }

    public Promise<Boolean> getPressed() throws IOException {
        final Promise<Boolean> promise = new Promise<>();
        Protocol.getSiValue(ev3.getPacketManager(), port, EV3_TOUCH, TOUCH_TOUCH, 1)
                .then(new Consumer<float[]>() {
                    @Override
                    public void call(float[] values) {
                        promise.resolve((int) values[0] == 1);
                    }
                });
        return promise;
    }


}
