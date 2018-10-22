package it.unive.dais.legodroid.lib.motors;

public class BaseMotor implements Motor {
    private int port;

    public BaseMotor(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void forward(int speed) {

    }

    @Override
    public void backward(int speed) {

    }

    @Override
    public void brake() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isMoving() {
        return false;
    }
}
