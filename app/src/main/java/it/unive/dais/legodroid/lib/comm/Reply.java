package it.unive.dais.legodroid.lib.comm;

import android.util.Log;

public class Reply extends Packet {
    private boolean error;

    public Reply(byte[] bytes) {
        counter = ((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff);
        error = bytes[2] != Const.DIRECT_COMMAND_SUCCESS;
        data = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, data, 0, data.length);
    }

    public boolean isError() {
        return error;
    }

}
