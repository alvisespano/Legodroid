package it.unive.dais.legodroid.app;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.GenEV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.Plug;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Prelude.ReTAG("MainActivity");

    private TextView textView;
    private final Map<String, Object> statusMap = new HashMap<>();
    @NonNull
    private TachoMotor motor1;

    private void updateStatus(@NonNull Plug p, @NonNull String key, @NonNull Object value) {
        Log.d(TAG, String.format("%s: %s: %s", p, key, value));
        statusMap.put(key, value);
        runOnUiThread(() -> textView.setText(statusMap.toString()));
    }

    private void setupEditable(@IdRes int id, @NonNull ThrowingConsumer<Integer, ?> f) {
        EditText e = findViewById(id);
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int x = 0;
                try {
                    x = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignored) {
                }
                f.accept(x);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        // create connection
        final BluetoothConnection conn = new BluetoothConnection("EV3");
        try {
            // actually connect via bluetooth
            final BluetoothConnection.BluetoothChannel ch = conn.connect(); // replace with your own brick name

            //GenEV3<MyCustomApi> ev3 = new GenEV3<>(ch);   // alternative with GenEV3
            final EV3 ev3 = new EV3(ch);

            Button stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(v -> {
                ev3.cancel();   // fire cancellation signal to the EV3 task
            });

            Button startButton = findViewById(R.id.startButton);
            startButton.setOnClickListener(v -> Prelude.trap(() -> ev3.run(this::legoMain)));
            // alternatively with GenEV3
            //startButton.setOnClickListener(v -> Prelude.trap(() -> ev3.run(this::legoMainCustomApi, MyCustomApi::new)));

            setupEditable(R.id.powerEdit, (n) -> {
                motor1.setPower(n);
                motor1.start();      // setPower() and setSpeed() require a call to start() afterwards in order to apply
            });
            setupEditable(R.id.speedEdit, (n) -> {
                motor1.setSpeed(n);
                motor1.start();
            });
        } catch (IOException e) {
            Log.e(TAG, String.format("fatal error: cannot connect to bluetooth device %s", conn.getName()));
            e.printStackTrace();
        }
    }

    // main program executed by EV3

    private void legoMain(EV3.Api api) {
        final String TAG = Prelude.ReTAG("legoMain");

        // get sensors
        final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
        final UltrasonicSensor ultraSensor = api.getUltrasonicSensor(EV3.InputPort._2);
        final TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
        final GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._4);

        // get motors
        motor1 = api.getTachoMotor(EV3.OutputPort.A);

        try {
            motor1.resetPosition();

            while (!api.ev3.isCancelled()) {    // loop until cancellation signal is fired
                // values returned by getters are boxed within a special CompletableFuture objects
                CompletableFuture<Float> gyro = gyroSensor.getAngle();
                updateStatus(gyroSensor, "gyro angle", gyro.get()); // call get() for actually reading the value - this may block!

                // you can safely subsume to the Future supertype as long as you only call the get() method
                Future<Short> ambient = lightSensor.getAmbient();
                updateStatus(lightSensor, "ambient", ambient.get());

                Future<Short> reflected = lightSensor.getReflected();
                updateStatus(lightSensor, "reflected", reflected.get());

                Future<Float> distance = ultraSensor.getDistance();
                updateStatus(ultraSensor, "distance", distance.get());

                Future<LightSensor.Color> colf = lightSensor.getColor();
                LightSensor.Color col = colf.get();
                updateStatus(lightSensor, "color", col);
                // when you need to deal with the UI, you must do it within a lambda passed to runOnUiThread()
                runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));

                Future<Boolean> touched = touchSensor.getPressed();
                updateStatus(touchSensor, "touch", touched.get() ? 1 : 0);

                Future<Float> pos = motor1.getPosition();
                updateStatus(motor1, "motor position", pos.get());

                // if you need more control over asynchronous execution, instead of using get() use CompletableFuture advanced methods
                lightSensor.getColor().thenCombineAsync(motor1.getPosition(), (color, position) -> {
                    // do something with color and position
                    Log.d(TAG, String.format("CompetableFuture example: color=%s and position=%s", color, position));
                    return (color.toARGB32() & 0x00ff0000) >> 16 >= 0x80 && position > 10.0;
                }).thenComposeAsync((Boolean b) -> {
                    int n = Prelude.trap((Integer n) -> { return touchSensor.getPressed(); });
                });
//                        .thenAcceptAsync((b -> {
//                            if (b) {
//
//                            }
//                        }))

//                motor1.setStepSpeed(20, 0, 5000, 0, true);
//                motor1.waitCompletion();
//                motor1.setStepSpeed(-20, 0, 5000, 0, true);
//                Log.d(TAG, "waiting for long motor operation completed...");
//                motor1.waitUntilReady();
//                Log.d(TAG, "long motor operation completed");
            }
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            Prelude.trap(motor1::stop);
        }

    }

    // the following part shows how to customize EV3.Api class by extending it and using GenEV3 generics appropriately
    // in order to get a strongly-typed subtype of EV3.Api
    //

    // alternative main with custom extension of EV3.Api
    private void legoMainCustomApi(MyCustomApi api) {
        final String TAG = Prelude.ReTAG("legoMainCustomApi");
        // specialized methods can be safely called
        api.mySpecialCommand();
        // stub the other main
        legoMain(api);
    }

    // in case you need to extend EV3.Api with new custom commands/methods
    private static class MyCustomApi extends EV3.Api {

        private MyCustomApi(@NonNull GenEV3<? extends EV3.Api> ev3) {
            super(ev3);
        }

        public void mySpecialCommand() {
        }
    }

}


