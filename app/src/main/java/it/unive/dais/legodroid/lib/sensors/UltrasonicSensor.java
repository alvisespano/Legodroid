package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.InputPort;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;


public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3.Api api, InputPort port) {
        super(api, port);
    }

    public Future<Float> getDistance() throws IOException {
        Future<float[]> f = api.getSiValue(port, Const.EV3_ULTRASONIC, Const.US_CM, 1);
        return api.execAsync(() -> f.get()[0]);
    }
}
