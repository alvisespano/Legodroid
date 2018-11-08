package it.unive.dais.legodroid.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView textView;

    // types of events

    private static class Stop implements EV3.Event {
    }

    private static class DataReady implements EV3.Event {
        public final int value;
        public final String header;

        public DataReady(String hd, int value) {
            this.header = hd;
            this.value = value;
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText("");

        try {
            BluetoothConnection conn = new BluetoothConnection("EV3");
            Channel channel = conn.connect();
            EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));

            ev3.setEventListener(event -> {
                if (event instanceof DataReady) {
                    DataReady e = (DataReady) event;
                    runOnUiThread(() -> {
                        textView.append(String.format("%s: %d\n", e.header, e.value));
                    });
                }
                if (event instanceof Stop) {
                    ev3.cancel();
                }
            });

            // main program executed by EV3

            ev3.run(api -> {
                LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
                TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
                TachoMotor motor1 = api.getTachoMotor(EV3.OutputPort.A);    // TODO: testare il comportamento quando non sono collegati davvero i motori/sensori alle porte
//                boolean running = true;

                while (!ev3.isCancelled()) {
                    try {
                        motor1.setSpeed(10);

                        Future<Short> ambient = lightSensor.getAmbient();
                        showData(api, "ambient", ambient.get());

                        Future<Short> reflected = lightSensor.getReflected();
                        showData(api, "reflected", reflected.get());

                        Future<LightSensor.Rgb> rgb = lightSensor.getRgb();
                        int rgbv = rgb.get().R << 16 | rgb.get().G << 8 | rgb.get().B;
                        showData(api, "RGB", rgbv);
                        Log.d(TAG, String.format("rgb: %d", rgbv));

                        Future<Boolean> touched = touchSensor.getPressed();
                        showData(api, "touch", touched.get() ? 1 : 0);

                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

//                    EV3.Event evt;
//                    while ((evt = api.pollEvents()) != null) {
//                        if (evt instanceof Stop) {
//                            running = false;
//                        }
//                    }
                }

            });

            Button button = findViewById(R.id.pollButton);
            button.setOnClickListener(v -> {
                ev3.sendEvent(new Stop());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showData(EV3.Api api, String hd, int x) {
        Log.d(TAG, String.format("%s: %d", hd, x));
        api.sendEvent(new DataReady(hd, x));
    }
}
