package it.unive.dais.legodroid.lib.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class BluetoothConnection implements Connection {
    private static final long READ_TIMEOUT_MS = 1000;

    @NonNull
    private final String name;
    @NonNull
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    @Nullable
    private BluetoothDevice device;
    @Nullable
    private BluetoothSocket socket;

    public BluetoothConnection(@NonNull String name) {
        this.name = name;
    }

    @Override
    public BluetoothChannel connect() throws IOException {
        if (!adapter.isEnabled())
            throw new IOException("bluetooth adapter is not enabled or unavailable");
        Set<BluetoothDevice> devs = adapter.getBondedDevices();
        boolean s = false;
        for (BluetoothDevice dev : devs) {
            if (dev.getName().equals(name)) {
                device = dev;
                s = true;
            }
        }
        if (!s)
            throw new IOException("brick not found");
        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        socket.connect();
        return new BluetoothChannel(socket);
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null)
            socket.close();
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }

    public static class BluetoothChannel implements Channel {
        @NonNull
        private InputStream in;
        @NonNull
        private OutputStream out;

        private BluetoothChannel(@NonNull BluetoothSocket socket) throws IOException {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        @Override
        public void write(Packet p) throws IOException {
            byte[] a = p.getBytes();
            byte[] l = new byte[]{(byte) (a.length & 0xFF), (byte) ((a.length >> 8) & 0xFF)};
            out.write(l);
            out.write(a);
        }

        @Override
        public Packet read() throws IOException, TimeoutException {
            byte[] lb = readSized(2);
            int len = ((lb[0] & 0xff) << 8) | (lb[1] & 0xff);
            return new DirectCommandReply(readSized(len));
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
    }


}
