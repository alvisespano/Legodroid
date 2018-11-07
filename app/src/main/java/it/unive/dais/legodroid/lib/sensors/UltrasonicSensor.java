package it.unive.dais.legodroid.lib.sensors;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

import java.io.IOException;
import java.util.concurrent.Future;


public class UltrasonicSensor extends AbstractSensor {
    public UltrasonicSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_ULTRASONIC);
    }

    public Future<Float> getDistance() throws IOException {
        return getSi1(Const.US_CM);
    }
}
