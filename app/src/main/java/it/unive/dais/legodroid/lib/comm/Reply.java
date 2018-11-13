package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public class Reply extends Packet {
    private final boolean error;

    public Reply(@NonNull byte[] bytes) {
        super(((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff), new byte[bytes.length - 3]);
        error = bytes[2] != Const.DIRECT_COMMAND_SUCCESS;
        System.arraycopy(bytes, 3, data, 0, data.length);
    }

    public boolean isError() {
        return error;
    }

    @NonNull
    public byte[] getData() {
        if (isError()) throw new IllegalStateException(String.format("reply returned error (counter = %d)", getCounter()));
        return data;
    }
}
