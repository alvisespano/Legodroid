package it.unive.dais.legodroid.lib;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Build;

import it.unive.dais.legodroid.lib.lowlevel.Connection;
import it.unive.dais.legodroid.lib.util.Promise;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class BluetoothConnection implements Connection {
    private static final long READ_TIMEOUT_MS = 1000;

    private final String deviceName;
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    public BluetoothConnection() {
        this("EV3");
    }

    public BluetoothConnection(String deviceName) {
        this.deviceName = deviceName;
    }

    private static class ReadTask extends AsyncTask<Integer, Void, byte[]> {
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
                    int l = in.read(data, 0, integers[0]);
                    return Arrays.copyOf(data, l);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void connect() throws IOException {
        if (!adapter.isEnabled())
            throw new IOException("Bluetooth adapter is not enabled or unavailable.");
        Set<BluetoothDevice> bind = adapter.getBondedDevices();
        boolean s = false;
        for (BluetoothDevice dev : bind) {
            if (dev.getName().equals(deviceName)) {
                device = dev;
                s = true;
            }
        }
        if (!s)
            throw new IOException("Brick not found.");
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

    private byte[] readSized(int size) throws IOException, TimeoutException {
        byte[] r = new byte[size];
        int off = 0;
        long now = System.currentTimeMillis();
        while (off < size) {
            off += in.read(r, off, size - off);
            if (System.currentTimeMillis() - now > READ_TIMEOUT_MS) throw new TimeoutException();
        }
        return r;
    }

    public byte[] readPacket() throws IOException, TimeoutException {
        byte[] lb = readSized(2);
        return readSized(((lb[0] & 0xff) << 8) | (lb[1] & 0xff));
    }

    public void writePacket(byte[] a) throws IOException {
        byte[] l = new byte[] { (byte) (a.length & 0xFF), (byte) ((a.length >> 8) & 0xFF) };
        out.write(l);
        out.write(a);
    }

}
