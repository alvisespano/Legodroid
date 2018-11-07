package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;


public class TouchSensor extends AbstractSensor {
    public TouchSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_TOUCH);
    }

    public Future<Boolean> getPressed() throws IOException {
        return getPercent1(Const.TOUCH_TOUCH, (x) -> x > 0);
    }
}
