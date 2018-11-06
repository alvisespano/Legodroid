package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Promise;



public class TouchSensor extends AbstractSensor {
    public TouchSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port);
    }
}
