package it.unive.dais.legodroid.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private final Map<String, Integer> statusMap = new HashMap<>();

    private void updateStatus(EV3 ev3, String key, int value) {
        Log.d(TAG, String.format("%s: %d", key, value));
        statusMap.put(key, value);
        runOnUiThread(() -> textView.setText(statusMap.toString())); //textView.append(String.format("%s: %d\n", e.key, e.value)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        try {
            final EV3 ev3 = new EV3(new BluetoothConnection("EV3").connect());

            Button startButton = findViewById(R.id.startButton);
            startButton.setOnClickListener(v -> {

                // main program executed by EV3

                ev3.run(api -> {

                    LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
                    TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
                    TachoMotor motor1 = api.getTachoMotor(EV3.OutputPort.A);    // TODO: testare il comportamento quando non sono collegati davvero i motori/sensori alle porte

                    {
                        EditText e = findViewById(R.id.motorEdit);
                        e.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                int speed = 0;
                                try {
                                    speed = Integer.parseInt(s.toString());
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, String.format("motor speed set to %d", speed));
                                try {
                                    motor1.setSpeed(speed);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });
                    }

                    while (!ev3.isCancelled()) {
                        try {
                            Future<Short> ambient = lightSensor.getAmbient();
                            updateStatus(ev3, "ambient", ambient.get());

                            Future<Short> reflected = lightSensor.getReflected();
                            updateStatus(ev3, "reflected", reflected.get());

                            Future<LightSensor.Rgb> rgb = lightSensor.getRgb();
                            int rgbv = rgb.get().R << 16 | rgb.get().G << 8 | rgb.get().B;
                            updateStatus(ev3, "rgb", rgbv);

                            Future<Boolean> touched = touchSensor.getPressed();
                            updateStatus(ev3, "touch", touched.get() ? 1 : 0);

                        } catch (IOException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                    }

                });
            });

            Button stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(v -> ev3.cancel());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
