package it.unive.dais.legodroid.lib.motors;

public interface Motor {
    int getPort();

    void forward(int speed);
    void backward(int speed);
    void brake();
    void stop();
    boolean isMoving();
}
