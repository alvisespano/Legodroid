package it.unive.dais.legodroid.lib.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import it.unive.dais.legodroid.lib.util.Prelude;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * This class implements a {@link Connection} between the Android device and the GenEV3 via the Bluetooth protocol.
 * Instances of this class do not represent an active connection but rather a factory for creating the actual connection channel.
 * The type of the channel is the inner class {@link BluetoothChannel}.
 */
public class BluetoothConnection implements Connection<BluetoothConnection.BluetoothChannel> {
    private static final String TAG = ReTAG("BluetoothConnection");

    @NonNull
    private final String name;
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    @Nullable
    private BluetoothDevice device = null;
    @Nullable
    private BluetoothSocket socket = null;

    /**
     * Create an object given the name of the GenEV3 device, as configured on the brick settings.
     * @param name the name of the GenEV3 device. LEGO factory settings default to "GenEV3".
     */
    public BluetoothConnection(@NonNull String name) {
        this.name = name;
    }

    /**
     * Create a channel for communication.
     * Multiple calls to this method do not produce multiple channels: only one active channel is supported.
     * This method internally performs the bluetooth discovery, searching among paired devices for the device whose name has been passed as argument to the constructor.
     * @apiNote The GenEV3 device must be first paired with the mobile Android device; refer to the device Bluetooth settings for more info.
     * @return an object of type {@link BluetoothChannel}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    @Override
    public BluetoothChannel connect() throws IOException {
        if (socket != null && socket.isConnected()) {
            Log.w(TAG, "bluetooth socket is already connected");
            return new BluetoothChannel(socket);
        }
        if (adapter == null)
            throw new IOException("bluetooth is not supported");
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

    /**
     * This inner non-static class represents an active bluetooth channel through which the two connected devices communicate sending commands and receiving replies.
     * @see Channel
     */
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
        public void send(@NonNull Command p) throws IOException {
            byte[] a = p.marshal();
            byte[] l = new byte[]{(byte) (a.length & 0xFF), (byte) ((a.length >> 8) & 0xFF)};
            byte[] w = Prelude.concat(l, a);
//            Log.d(TAG, String.format("send: { %s }", Prelude.bytesToHex(w)));
            out.write(w);
        }

        @NonNull
        @Override
        public Reply receive() throws IOException {
            byte[] lb = readSized(2);
            int len = ((lb[1] & 0xff) << 8) | (lb[0] & 0xff);
//            Log.d(TAG, String.format("receive len = %d", len));
            return new Reply(readSized(len));
        }

        @NonNull
        private byte[] readSized(int size) throws IOException {
            byte[] r = new byte[size];
            int off = 0;
            while (off < size) {
//                Log.d(TAG, "reading...");
                off += in.read(r, off, size - off);
//                Log.d(TAG, String.format("receive: %s", Prelude.bytesToHex(r)));
            }
            return r;
        }

        @Override
        public void close() {
            if (socket != null) {
                Log.v(TAG, String.format("bluetooth disconnected from device '%s'", Objects.requireNonNull(device).getName()));
                Prelude.trap(socket::close);
            }
        }
    }


}
