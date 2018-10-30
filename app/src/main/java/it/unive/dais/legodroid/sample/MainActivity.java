package it.unive.dais.legodroid.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            BluetoothConnection conn = new BluetoothConnection("EV3");
            Channel channel = null;
            channel = conn.connect();
            EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));

            ev3.run(api -> {
                LightSensor sen = api.getLightSensor(EV3.InputPort._3);
                TachoMotor motorX = api.getTachoMotor(EV3.OutputPort.A);
                TachoMotor motorY = api.getTachoMotor(EV3.OutputPort.B);

                int scanWidth = 10;
                int scanHeight = 10;
                int[][] buff = new int[scanWidth][scanHeight];

                for (int x = 0; x < scanWidth; x++) {
                    motorX.goToPositionAbs(x);

                    for (int y = 0; y < scanHeight; y++) {
                        try {
                            motorY.goToPositionAbs(y);
                            FutureTask<Integer> pct = sen.getReflected();
                            Log.d(TAG, String.format("reflected: %d", pct.get()));
                            buff[x][y] = pct.get();
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

//                        ScannerEvent evt = null;
//                        while ((evt = api.pollEvents()) != null) {
//                            if (evt.getEventType() == ScannerEventType.SCANNER_STOP) {
//                                return;
//                            }
//                        }
                    }
                }
            });

            Button button = findViewById(R.id.pollButton);
            button.setOnClickListener(v -> {
//                api.sendEvent(new ScannerEvent(ScannerEventType.SCANNER_STOP));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
