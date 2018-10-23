package com.giuliozausa.ev3droid.sensors.touch;

import com.giuliozausa.ev3droid.EV3;
import com.giuliozausa.ev3droid.lowlevel.ProgramGen;
import com.giuliozausa.ev3droid.sensors.BaseSensor;
import com.giuliozausa.ev3droid.util.Handler;
import com.giuliozausa.ev3droid.util.Promise;

import java.io.IOException;

import static com.giuliozausa.ev3droid.lowlevel.Constants.EV3_TOUCH;
import static com.giuliozausa.ev3droid.lowlevel.Constants.TOUCH_TOUCH;

public class TouchSensor extends BaseSensor<Object> {
    public TouchSensor(EV3 ev3, int port) {
        super(ev3, port, null);
    }

    public Promise<Boolean> getPressed() throws IOException {
        final Promise<Boolean> promise = new Promise<>();
        ProgramGen.getSiValue(ev3.getPacketManager(), port, EV3_TOUCH, TOUCH_TOUCH, 1)
                .then(new Handler<float[]>() {
                    @Override
                    public void call(float[] values) {
                        promise.resolve((int) values[0] == 1);
                    }
                });
        return promise;
    }
}
