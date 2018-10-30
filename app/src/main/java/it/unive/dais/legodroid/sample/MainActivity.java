package it.unive.dais.legodroid.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import it.unive.dais.legodroid.lib.sensors.LightSensor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
            Channel channel = null;
            channel = conn.connect();
            EV3 ev3 = new EV3(new SpooledAsyncChannel(channel), this);

            ev3.setEventListener(event -> {
                if (event instanceof DataReady) {
                    DataReady e = (DataReady) event;
                    TextView view = findViewById(R.id.textView);
                    view.setText(String.format("%d", e.value));
                }
            });

            ev3.run(api -> {
                LightSensor sen = api.getLightSensor(EV3.InputPort._3);
                boolean running = true;

                while (running) {
                    try {
                        Future<Integer> pct = sen.getReflected();
                        api.sendEvent(new DataReady(pct.get()));
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


}
