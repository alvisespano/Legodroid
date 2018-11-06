# Legodroid

A small library for communicating with the Lego EV3 educational kit from Android.

## Usage

First, you have to open the bluetooth connection. Bluetooth device must be already paired from Android Bluetooth Settings.

```java
BluetoothConnection conn = new BluetoothConnection("EV3");
Channel channel = conn.connect();
EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));
```

Then, you can start a new job to run on the EV3. It will be run on another thread, indipendently from you application.

```java
ev3.run(() -> {
  try {
    TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);

    boolean running = true;
    while (running) {
      // Set motor speed to 10 (port A)
      api.getTachoMotor(EV3.OutputPort.A).setSpeed(10);

      // Stop job and motor if pressed
      if (touchSensor.getPressed().get()) {
        api.getTachoMotor(EV3.OutputPort.A).setSpeed(0);
        this.running = false;
      }
    }
  } catch (IOException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
  }
});
```
