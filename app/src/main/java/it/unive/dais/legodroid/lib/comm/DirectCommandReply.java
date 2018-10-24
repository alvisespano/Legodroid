package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public class DirectCommandReply implements Packet {
    private int length;
    private int counter;
    private boolean error;
    @NonNull
    private byte[] data;

    DirectCommandReply(byte[] bytes) {
        length = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
        counter = ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
        error = bytes[4] == Constants.DIRECT_COMMAND_SUCCESS;
        data = new byte[length - 3];
        System.arraycopy(bytes, 5, data, 0, data.length);
    }

    public int getLength() {
        return length;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public byte[] getBytes() {
        return data;
    }
}
