package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.InputPort;
import it.unive.dais.legodroid.lib.comm.Const;


public class GyroSensor extends AbstractSensor {
    public GyroSensor(EV3.Api api, InputPort port) {
        super(api, port);
    }

    public Future<Float> getAngle() throws IOException {
        Future<float[]> f = api.getSiValue(port, Const.EV3_GYRO, Const.GYRO_ANGLE, 1);
        return api.execAsync(() -> f.get()[0]);
    }

    public Future<Float> geRate() throws IOException {
        Future<float[]> f = api.getSiValue(port, Const.EV3_GYRO, Const.GYRO_RATE, 1);
        return api.execAsync(() -> f.get()[0]);
    }
}
