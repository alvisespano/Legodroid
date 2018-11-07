package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;


public class GyroSensor extends AbstractSensor {
    public GyroSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port, Const.EV3_GYRO);
    }

    public Future<Float> getAngle() throws IOException {
        return getSi1(Const.GYRO_ANGLE);
    }

    public Future<Float> getRate() throws IOException {
        return getSi1(Const.GYRO_RATE);
    }
}
