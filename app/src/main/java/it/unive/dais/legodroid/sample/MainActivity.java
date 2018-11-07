package it.unive.dais.legodroid.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Function;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // types of events
    //

    private static class Stop implements EV3.Event {
    }

    private static class DataReady implements EV3.Event {
        public final int value;

        public DataReady(int value) {
            this.value = value;
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            BluetoothConnection conn = new BluetoothConnection("EV3");
            Channel channel = conn.connect();
            EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));

            ev3.setEventListener(event -> {
                if (event instanceof DataReady) {
                    DataReady e = (DataReady) event;
                    runOnUiThread(() -> {
                        TextView textView = findViewById(R.id.textView);
                        textView.append(String.format("%d", e.value));
                    });
                }
                if (event instanceof Stop) {
                    ev3.stop(); // TODO: finire meccanismo di stop dentro Ev3 e Api
                }
            });

            // main program executed by EV3
            //

            ev3.run(api -> {
                LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
                TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
                TachoMotor motor1 = api.getTachoMotor(EV3.OutputPort.A);
                boolean running = true;

                while (running) {
                    try {
                        motor1.setSpeed(10);

                        Future<Short> ambient = lightSensor.getAmbient();
                        showData(api, ambient.get());

                        Future<Short> reflected = lightSensor.getReflected();
                        showData(api, reflected.get());

                        Log.d(TAG, String.format("reflected: %d", reflected.get()));

                        Future<LightSensor.Rgb> rgb = lightSensor.getRgb();
                        int rgbv = rgb.get().R << 16 | rgb.get().G << 8 | rgb.get().B;
                        showData(api, rgbv);
                        Log.d(TAG, String.format("rgb: %d", rgbv));

                        Future<Boolean> touched = touchSensor.getPressed();
                        showData(api, touched.get() ? 1 : 0);

                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    EV3.Event evt;
                    while ((evt = api.pollEvents()) != null) {
                        if (evt instanceof Stop) {
                            running = false;
                        }
                    }
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

    private void showData(EV3.Api api, int x) {
        api.sendEvent(new DataReady(x));
    }
}
