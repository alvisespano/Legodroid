package it.unive.dais.legodroid.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.Packet;
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
            Channel<Packet, Packet> channel = conn.connect();
            EV3 ev3 = new EV3(new SpooledAsyncChannel(channel));

//            ev3.run((Api api) -> {
//                ColorSensor s = api.getColorSensor(0);
//                Color[][] bitmap = ...
//                for (int x = 0; x < w; x++) {
//                    for (int y = 0; y < h; y++) {
//                        api.moveTo(x, y);
//                        Color c = s.get();
//                        bitmap[x][y] = c;
//                    }
//
//                    ev3.setEventListener(event -> {
//                        if (event.getEventType() == ScannerEventType.SCANNER_FINISHED) {
//                            displayScannedData(event.scanned);
//                        }
//                    });

            ev3.run(api -> {
                LightSensor lightSensor = api.getLightSensor(0);
                TachoMotor motorX = api.getTachoMotor(0);
                TachoMotor motorY = api.getTachoMotor(1);

                int scanWidth = 10;
                int scanHeight = 10;
                int[][] scanBuffer = new int[scanWidth][scanHeight];

                for (int x = 0; x < scanWidth; x++) {
                    motorX.goToPositionAbs(x);

                    for (int y = 0; y < scanHeight; y++) {
                        motorY.goToPositionAbs(y);
                        int color = lightSensor.getReflected();
                        scanBuffer[x][y] = color;

//                        ScannerEvent evt = null;
//                        while ((evt = api.pollEvents()) != null) {
//                            if (evt.getEventType() == ScannerEventType.SCANNER_STOP) {
//                                return;
//                            }
//                        }
                    }
                }
            });

            final Button button = findViewById(R.id.pollButton);
            button.setOnClickListener(v -> {
//                ev3.sendEvent(new ScannerEvent(ScannerEventType.SCANNER_STOP));
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
