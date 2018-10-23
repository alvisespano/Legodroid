package it.unive.dais.legodroid.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.android.AndroidBluetoothConnector;
import it.unive.dais.legodroid.lib.sensors.touch.TouchSensor;
import it.unive.dais.legodroid.lib.util.Handler;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private AndroidBluetoothConnector connector;
    private EV3 ev3;
    private TouchSensor touchSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            connector = new AndroidBluetoothConnector();
            connector.connect();
            ev3 = new EV3(connector);
            touchSensor = ev3.createTouchSensor(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Button button = findViewById(R.id.pollButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView label = findViewById(R.id.textView);
                try {
                    touchSensor.getPressed().then(new Handler<Boolean>() {
                        @Override
                        public void call(Boolean data) {
                            Log.i("ev3", data.toString());
                            label.setText(data.toString());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            connector.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
