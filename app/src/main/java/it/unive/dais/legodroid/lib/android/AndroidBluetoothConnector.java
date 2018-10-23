package it.unive.dais.legodroid.lib.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Build;
import it.unive.dais.legodroid.lib.lowlevel.Connector;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class AndroidBluetoothConnector implements Connector {
    private final String deviceName;
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    public AndroidBluetoothConnector() {
        this.deviceName = "EV3";
    }

    public AndroidBluetoothConnector(String deviceName) {
        this.deviceName = deviceName;
    }

    static class ReadTask extends AsyncTask<Integer, Void, byte[]> {
        private BluetoothSocket socket;
        private InputStream in;

        public ReadTask(BluetoothSocket socket, InputStream in) {
            this.socket = socket;
            this.in = in;
        }

        @Override
        protected byte[] doInBackground(Integer... integers) {
            try {
                if (socket.isConnected()) {
                    while (in.available() == 0) ;
                    byte[] data = new byte[integers[0]];
                    int sizeRead = in.read(data, 0, integers[0]);
                    return Arrays.copyOf(data, sizeRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void connect() throws Exception {
        if (!adapter.isEnabled())
            throw new Exception("Bluetooth is not usable.");
        Set<BluetoothDevice> bind = adapter.getBondedDevices();
        boolean s = false;
        for (BluetoothDevice dev : bind) {
            if (dev.getName().equals(deviceName)) {
                device = dev;
                s = true;
            }
        }
        if (!s)
            throw new Exception("Brick not found.");
        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        socket.connect();
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public Promise<Void> write(byte[] data) throws IOException {
        out.write(data);
        return new Promise<>(); // TODO
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public Promise<byte[]> read(int size) {
        Promise<byte[]> promise = new Promise<>();
        ReadTask task = new ReadTask(socket, in);
        task.doInBackground(size);
        return promise;
    }
}
