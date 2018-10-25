package it.unive.dais.legodroid.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.comm.AndroidBluetoothConnection;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.motors.TachoMotor;
import it.unive.dais.legodroid.lib.sensors.LightSensor;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private enum ScannerEventType {
        SCANNER_STOP,
        SCANNER_FINISHED
    }

    private static class ScannerEvent {
        private ScannerEventType eventType;
        private int[][] scanned;

        public ScannerEvent(ScannerEventType eventType) {
            this.eventType = eventType;
        }

        public ScannerEventType getEventType() {
            return eventType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidBluetoothConnection conn = new AndroidBluetoothConnection();
        try {
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final EV3<ScannerEvent> ev3 = new EV3<>(conn);

        ev3.setEventListener(event -> {
            if (event.getEventType() == ScannerEventType.SCANNER_FINISHED) {
                displayScannedData(event.scanned);
            }
        });

        ev3.run(api -> {
            LightSensor cSensor = api.getLightSensor(0);
            TachoMotor motorX = api.getTachoMotor(0);
            TachoMotor motorY = api.getTachoMotor(1);

            int scanWidth = 10;
            int scanHeight = 10;
            int[][] scanBuffer = new int[scanWidth][scanHeight];

            for (int x = 0; x < scanWidth; x++) {
                motorX.goToPositionAbs(x);

                for (int y = 0; y < scanHeight; y++) {
                    motorY.goToPositionAbs(y);
                    int color = cSensor.getReflected();
                    scanBuffer[x][y] = color;

                    ScannerEvent evt = null;
                    while ((evt = api.pollEvents()) != null) {
                        if (evt.getEventType() == ScannerEventType.SCANNER_STOP) {
                            return;
                        }
                    }
                }
            }


        });

        final Button button = findViewById(R.id.pollButton);
        button.setOnClickListener(v -> {
            ev3.sendEvent(new ScannerEvent(ScannerEventType.SCANNER_STOP));
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
