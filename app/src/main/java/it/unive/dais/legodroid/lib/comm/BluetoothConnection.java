package it.unive.dais.legodroid.lib.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import it.unive.dais.legodroid.lib.util.Prelude;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

public class BluetoothConnection implements Connection {
    private static final String TAG = ReTAG("BluetoothConnection");
    private static final long READ_TIMEOUT_MS = 10000;

    @NonNull
    private final String name;
    @NonNull
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    @Nullable
    private BluetoothDevice device = null;
    @Nullable
    private BluetoothSocket socket = null;

    public BluetoothConnection(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public BluetoothChannel connect() throws IOException {
        if (socket != null && socket.isConnected()) {
            Log.w(TAG, "bluetooth socket is already connected");
            return new BluetoothChannel(socket);
        }
        if (!adapter.isEnabled())
            throw new IOException("bluetooth adapter is not enabled or unavailable");
        Set<BluetoothDevice> devs = adapter.getBondedDevices();
        for (BluetoothDevice dev : devs) {
            if (dev.getName().equals(name)) {
                device = dev;
                break;
            }
        }
        if (device == null)
            throw new IOException("brick not found");
        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        socket.connect();
        Log.v(TAG, String.format("bluetooth connected successfully to device '%s'", device.getName()));
        return new BluetoothChannel(socket);
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null) {
            Log.v(TAG, String.format("bluetooth disconnected from device '%s'", Objects.requireNonNull(device).getName()));
            socket.close();
        }
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }

    public class BluetoothChannel implements Channel {
        private final String TAG = ReTAG(BluetoothConnection.TAG, ".BluetoothChannel");
        @NonNull
        private final InputStream in;
        @NonNull
        private final OutputStream out;

        private BluetoothChannel(@NonNull BluetoothSocket socket) throws IOException {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        @Override
        public void write(@NonNull Command p) throws IOException {
            byte[] a = p.marshal();
            byte[] l = new byte[]{(byte) (a.length & 0xFF), (byte) ((a.length >> 8) & 0xFF)};
            byte[] w = Prelude.concat(l, a);
//            Log.d(TAG, String.format("write: { %s }", Prelude.bytesToHex(w)));
            out.write(w);
        }

        @NonNull
        @Override
        public Reply read() throws IOException {
            byte[] lb = readSized(2);
            int len = ((lb[1] & 0xff) << 8) | (lb[0] & 0xff);
//            Log.d(TAG, String.format("read len = %d", len));
            return new Reply(readSized(len));
        }

        @NonNull
        private byte[] readSized(int size) throws IOException {
            byte[] r = new byte[size];
            int off = 0;
            while (off < size) {
//                Log.d(TAG, "reading...");
                off += in.read(r, off, size - off);
//                Log.d(TAG, String.format("read: %s", Prelude.bytesToHex(r)));
            }
            return r;
        }

        @Override
        public void close() throws Exception {
            disconnect();
        }
    }


}
