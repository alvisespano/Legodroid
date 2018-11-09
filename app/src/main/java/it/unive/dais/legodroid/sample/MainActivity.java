package it.unive.dais.legodroid.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Const.ReTAG("MainActivity");

    private TextView textView;

    // types of events

    private static class DataReady implements EV3.Event {
        public final int value;
        public final String key;

        public DataReady(String hd, int value) {
            this.key = hd;
            this.value = value;
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        try {
            final EV3 ev3 = new EV3(new BluetoothConnection("EV3").connect());
            final Map<String, Integer> map = new HashMap<>();

            ev3.setEventListener(event -> {
                if (event instanceof DataReady) {
                    DataReady e = (DataReady) event;
                    map.put(e.key, e.value);
                    runOnUiThread(() -> textView.setText(map.toString())); //textView.append(String.format("%s: %d\n", e.key, e.value)));
                }
            });

            // main program executed by EV3

            ev3.run(api -> {
                LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
                TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
                TachoMotor motor1 = api.getTachoMotor(EV3.OutputPort.A);    // TODO: testare il comportamento quando non sono collegati davvero i motori/sensori alle porte

                while (!ev3.isCancelled()) {
                    try {
                        motor1.setSpeed(10);

                        Future<Short> ambient = lightSensor.getAmbient();
                        showData(ev3, "ambient", ambient.get());

                        Future<Short> reflected = lightSensor.getReflected();
                        showData(ev3, "reflected", reflected.get());

                        Future<LightSensor.Rgb> rgb = lightSensor.getRgb();
                        int rgbv = rgb.get().R << 16 | rgb.get().G << 8 | rgb.get().B;
                        showData(ev3, "rgb", rgbv);

                        Future<Boolean> touched = touchSensor.getPressed();
                        showData(ev3, "touch", touched.get() ? 1 : 0);

                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                }
                Log.i(TAG, "exiting EV3 program");

            });

            Button button = findViewById(R.id.stopButton);
            button.setOnClickListener(v -> ev3.cancel());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showData(EV3 ev3, String key, int x) {
        Log.d(TAG, String.format("%s: %d", key, x));
        ev3.sendEvent(new DataReady(key, x));
    }
}
