package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.InputPort;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;


public class TouchSensor extends AbstractSensor {
    public TouchSensor(EV3.Api api, InputPort port) {
        super(api, port);
    }

    public Future<Boolean> getPressed() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_TOUCH, Const.TOUCH_TOUCH, 1);
        return api.execAsync(() -> f.get()[0] > 0);
    }
}
