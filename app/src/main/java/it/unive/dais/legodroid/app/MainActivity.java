package it.unive.dais.legodroid.app;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.Plug;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Prelude.ReTAG("MainActivity");

    private TextView textView;
    private final Map<String, Object> statusMap = new HashMap<>();
    @Nullable
    private TachoMotor motor;   // this is a class field because we need to access it from multiple methods

    private void updateStatus(@NonNull Plug p, String key, Object value) {
        Log.d(TAG, String.format("%s: %s: %s", p, key, value));
        statusMap.put(key, value);
        runOnUiThread(() -> textView.setText(statusMap.toString()));
    }

    private void setupEditable(@IdRes int id, Consumer<Integer> f) {
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
                f.call(x);
            }
        });
    }

    // example of custom API
    private class AnotherCustomApi extends CustomApi {
        private AnotherCustomApi(@NonNull EV3 ev3) {
            super(ev3);
        }
    }

    private class ExtCustomApi extends CustomApi {
        private ExtCustomApi(@NonNull EV3 ev3) {
            super(ev3);
        }
    }

    // example of custom API
    private class CustomApi extends EV3.Api {

        private CustomApi(@NonNull EV3 ev3) {
            super(ev3);
        }

        @Override
        @NonNull
        public AveragingLightSensor getLightSensor(@NonNull EV3.InputPort port) {
            return new AveragingLightSensor(this, port);
        }
    }

    static class AveragingLightSensor extends LightSensor {
        @NonNull
        private final float[] avgHSV = new float[]{0.5f, 0.5f, 0.5f};
        @NonNull
        private final Map<Color, Integer> map = new HashMap<>();

        AveragingLightSensor(@NonNull CustomApi api, @NonNull EV3.InputPort port) {
            super(api, port);
            for (Color c : Color.values()) {    // populate map
                map.put(c, c.toARGB32());
            }
        }

        @NonNull
        private Color nearest(int rgb) {
            Pair<Integer, Map.Entry<Color, Integer>> min =
                    new Pair<>(0x100 * 3, map.entrySet().iterator().next());
            for (Map.Entry<Color, Integer> e : map.entrySet()) {
                final int v = e.getValue(), d = rgbDistance(v, rgb);
                if (min.first < d)
                    min = new Pair<>(d, e);
            }
            return min.second.getKey();
        }

        private static int rgbDistance(int x, int y) {
            int r = Math.abs(x & 0xff0000 >> 16 - y & 0xff0000 >> 16),
                    g = Math.abs(x & 0x00ff00 >> 8 - y & 0x00ff00 >> 8),
                    b = Math.abs(x & 0x0000ff - y & 0x0000ff);
            return r + g + b;
        }

        @Override
        @NonNull
        public Future<Color> getColor() {
            return api.execAsync(() -> {
                Future<Color> c = super.getColor();
                float[] hsv = new float[3];
                android.graphics.Color.colorToHSV(c.get().toARGB32(), hsv);
                for (int i = 0; i < 3; ++i)
                    avgHSV[i] = (avgHSV[i] + hsv[i]) / 2.f;
                return nearest(android.graphics.Color.HSVToColor(avgHSV));
            });
        }
    }

    // quick wrapper for accessing the private field MainActivity.motor only when not-null; also ignores any exception thrown
    private void applyMotor(@NonNull ThrowingConsumer<TachoMotor, Throwable> f) {
        if (motor != null)
            Prelude.trap(() -> f.call(motor));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        try {
            // call to EV3 via bluetooth
            BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("EV3").call(); // replace with your own brick name
            EV3 ev3 = new EV3(ch);

            Button stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(v -> {
                ev3.cancel();   // fire cancellation signal to the EV3 task
            });

            Button startButton = findViewById(R.id.startButton);

            ev3.run(this::legoMain, CustomApi::new);
            ev3.run(this::legoMain, AnotherCustomApi::new);
            ev3.run(this::extCustomLegoMain, ExtCustomApi::new);
            ev3.run(this::extCustomLegoMain, CustomApi::new);
            ev3.run(this::anotherCustomLegoMain, ExtCustomApi::new);

            ev3.run(this::legoMain, AnotherCustomApi::new);
            ev3.run(this::customLegoMain, AnotherCustomApi::new);
            ev3.run(this::legoMain, ExtCustomApi::new);
            ev3.run(this::extCustomLegoMain, ExtCustomApi::new);

//            String[] peers = new String[] { "MyBrickName1", "MyBrickName2", "MyBrickName3" };
//            Stream<? extends Channel<?>> r = Arrays.stream(peers).map(BluetoothConnection::new).map(BluetoothConnection::call);

            startButton.setOnClickListener(v -> Prelude.trap(() -> ev3.run(this::customLegoMain, CustomApi::new)));

            setupEditable(R.id.powerEdit, (x) -> applyMotor((m) -> {
                m.setPower(x);
                m.start();      // setPower() and setSpeed() require call to start() afterwards
            }));
            setupEditable(R.id.speedEdit, (x) -> applyMotor((m) -> {
                m.setSpeed(x);
                m.start();
            }));
        } catch (IOException e) {
            Log.e(TAG, "fatal error: cannot call to EV3");
            e.printStackTrace();
        } catch (EV3.AlreadyRunningException e) {
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
        motor = api.getTachoMotor(EV3.OutputPort.A);

        try {
            applyMotor(TachoMotor::resetPosition);

            while (!api.ev3.isCancelled()) {    // loop until cancellation signal is fired
                try {
                    // values returned by getters are boxed within a Future object
                    Future<Float> gyro = gyroSensor.getAngle();
                    updateStatus(gyroSensor, "gyro angle", gyro.get()); // call get() for actually reading the value - this may block!

                    Future<Short> ambient = lightSensor.getAmbient();
                    updateStatus(lightSensor, "ambient", ambient.get());

                    Future<Short> reflected = lightSensor.getReflected();
                    updateStatus(lightSensor, "reflected", reflected.get());

                    Future<Float> distance = ultraSensor.getDistance();
                    updateStatus(ultraSensor, "distance", distance.get());

                    Future<LightSensor.Color> colf = lightSensor.getColor();
                    LightSensor.Color col = colf.get();
                    updateStatus(lightSensor, "color", col);
                    // when you need to deal with the UI, you must do it via runOnUiThread()
                    runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));

                    Future<Boolean> touched = touchSensor.getPressed();
                    updateStatus(touchSensor, "touch", touched.get() ? 1 : 0);

                    Future<Float> pos = motor.getPosition();
                    updateStatus(motor, "motor position", pos.get());

                    Future<Float> speed = motor.getSpeed();
                    updateStatus(motor, "motor speed", speed.get());

                    motor.setStepSpeed(20, 0, 5000, 0, true);
                    motor.waitCompletion();
                    motor.setStepSpeed(-20, 0, 5000, 0, true);
                    Log.d(TAG, "waiting for long motor operation completed...");
                    motor.waitUntilReady();
                    Log.d(TAG, "long motor operation completed");

                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            applyMotor(TachoMotor::stop);
        }
    }

    // alternative version of the lego main with a custom API
    private void customLegoMain(CustomApi api) {
        final String TAG = Prelude.ReTAG("legoMainCustomApi");
        // stub the other main
        legoMain(api);
    }

    private void extCustomLegoMain(ExtCustomApi api) {
        final String TAG = Prelude.ReTAG("legoMainCustomApi");
        // stub the other main
        legoMain(api);
    }

    private void anotherCustomLegoMain(AnotherCustomApi api) {
        final String TAG = Prelude.ReTAG("anotherCustomLegoMain");
        // stub the other main
        legoMain(api);
    }

}


