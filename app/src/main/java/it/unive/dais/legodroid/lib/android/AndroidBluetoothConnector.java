package it.unive.dais.legodroid.lib.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import it.unive.dais.legodroid.lib.lowlevel.Connector;
import it.unive.dais.legodroid.lib.lowlevel.DataReceiveListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class AndroidBluetoothConnector implements Connector {
    private LinkedList<DataReceiveListener> onDataReceive = new LinkedList<>();
    private final String deviceName;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    public AndroidBluetoothConnector() {
        this("EV3");
    }

    public AndroidBluetoothConnector(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void connect() throws Exception {
        if (!adapter.isEnabled())
            throw new Exception("Bluetooth is not unable.");
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
    public void write(byte[] data) throws IOException {
        out.write(data);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void pollInput() throws IOException {
        while (socket.isConnected()) {
            if (in.available() != 0) {
                int len = in.read() + in.read() << 8;
                byte[] bytes = new byte[len];
                in.read(bytes);
                for (DataReceiveListener listener : onDataReceive) {
                    listener.onDataReceive(bytes);
                }
            }
        }
    }

    public void addDataReceiveListener(DataReceiveListener listener) {
        if (!onDataReceive.contains(listener))
            onDataReceive.add(listener);
    }

    public void removeDataReceiveListener(DataReceiveListener listener) {
        onDataReceive.remove(listener);
    }
}
