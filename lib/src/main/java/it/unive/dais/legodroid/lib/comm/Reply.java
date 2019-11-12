package it.unive.dais.legodroid.lib.comm;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 * This class implements a reply for the GenEV3 device.
 */
public class Reply extends Packet {
    private static final String TAG = "Reply";
    private final boolean error;

    /**
     * Create a reply object given the raw byte array received by the GenEV3 device.
     * This constructor acts as a deserializer.
     *
     * @param bytes the input byte array
     */
    public Reply(@NonNull byte[] bytes) {
        super(((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff), new byte[bytes.length - 3]);
        error = bytes[2] != Const.DIRECT_COMMAND_SUCCESS;
        System.arraycopy(bytes, 3, data, 0, data.length);
    }

    /**
     * Get the error flag as replied by the GenEV3 device.
     *
     * @return true if an error occurred; false otherwise.
     */
    public boolean isError() {
        return error;
    }

    /**
     * Get the data array of bytes.
     *
     * @return the array of bytes.
     * @implNote this implementation logs when the error flag is set; execution is not interrupted.
     */
    @NonNull
    @Override
    public byte[] getData() {
        if (isError())
            Log.e(TAG, String.format("error on reply #%d", counter)); //throw new IllegalStateException(String.format("reply returned error (counter = %d)", getCounter()));
        return data;
    }
}
