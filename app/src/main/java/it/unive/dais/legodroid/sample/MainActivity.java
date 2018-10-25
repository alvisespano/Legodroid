package it.unive.dais.legodroid.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import android.widget.TextView;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.Api;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothConnection conn = new BluetoothConnection("EV3");
        Channel<?, ?> ch = conn.connect();

        EV3 ev3 = new EV3(conn);
        ev3.run((Api api) -> {
            ColorSensor s = api.getColorSensor(0);
            Color[][] bitmap = ...
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    api.moveTo(x, y);
                    Color c = s.get();
                    bitmap[x][y] = c;
                }
            }
            api.runOnUiThread(() -> displayBitmap(bitmap));
        });

        TouchSensor touchSensor = ev3.createTouchSensor(0);

        final Button button = findViewById(R.id.pollButton);


        button.setOnClickListener(v ->

                Box<Boolean> b = touchSensor.getPressed();

                if (b.get())

                touchSensor.fetchPressed().then((boolean pressed) -> {
                    Log.i(TAG, String.format("touchSensor: %b", pressed));
                    final TextView label = findViewById(R.id.textView);
                    label.setText(pressed ? "pressed" : "released");
                }));
    }

}
