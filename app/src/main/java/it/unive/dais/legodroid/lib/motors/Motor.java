package it.unive.dais.legodroid.lib.motors;

import it.unive.dais.legodroid.lib.EV3;

public interface Motor {
    EV3.OutputPort getPort();

    void forward(int speed);
    void backward(int speed);
    void brake();
    void stop();
    boolean isMoving();
}
