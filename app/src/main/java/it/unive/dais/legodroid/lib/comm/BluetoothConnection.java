package it.unive.dais.legodroid.lib.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
        private static final String TAG = "BluetoothChannel";
        @NonNull
        private InputStream in;
        @NonNull
        private OutputStream out;

        private BluetoothChannel(@NonNull BluetoothSocket socket) throws IOException {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        private static byte[] concat(byte[] first, byte[] second) {
            byte[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }

        private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
        private static String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        @Override
        public void write(Command p) throws IOException {
            byte[] a = p.marshal();
            byte[] l = new byte[]{(byte) (a.length & 0xFF), (byte) ((a.length >> 8) & 0xFF)};
            byte[] w = concat(l, a);
            Log.d(TAG, String.format("write: { %s }", bytesToHex(w)));
            out.write(w);
        }

        @Override
        public Reply read() throws IOException, TimeoutException {
//            Log.d(TAG, "read loop");
//            while (true) {
//                int b = in.read();
//                Log.d(TAG, String.format("read: %d", b));
//            }

            byte[] lb = readSized(2);
            int len = ((lb[1] & 0xff) << 8) | (lb[0] & 0xff);
            Log.d(TAG, String.format("read len = %d", len));
            return new Reply(readSized(len));
        }

        private byte[] readSized(int size) throws IOException, TimeoutException {
            byte[] r = new byte[size];
            int off = 0;
            long now = System.currentTimeMillis();
            while (off < size) {
                off += in.read(r, off, size - off);
                Log.d(TAG, String.format("read: %s", bytesToHex(r)));
                if (System.currentTimeMillis() - now > READ_TIMEOUT_MS) throw new TimeoutException();
            }
            Log.d(TAG, String.format("read: full: %s", bytesToHex(r)));
            return r;
        }
    }


}
