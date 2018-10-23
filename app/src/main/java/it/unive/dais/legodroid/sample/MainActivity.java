package it.unive.dais.legodroid.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.AndroidBluetoothConnector;
import it.unive.dais.legodroid.lib.lowlevel.Connector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Connector connector = new AndroidBluetoothConnector();
            connector.connect();
            final EV3 ev3 = new EV3(connector);

            final Button button = findViewById(R.id.pollButton);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//                    final TouchSensor touchSensor = new TouchSensor(ev3, 0);
//                    final TextView label = findViewById(R.id.textView);
//                    try {
//                        touchSensor.getPressed().then(new Handler<Boolean>() {
//                            @Override
//                            public void call(Boolean data) {
//                                Log.i("ev3", data.toString());
//                                label.setText(data.toString());
//                            }
//                        });
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    try {
                        ev3.soundTone(2, 1000, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // connector.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
