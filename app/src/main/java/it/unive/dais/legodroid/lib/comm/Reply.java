package it.unive.dais.legodroid.lib.comm;

public class Reply extends Packet {
    private boolean error;

    public Reply(byte[] bytes) {
        counter = ((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff);
        error = bytes[2] != Const.DIRECT_COMMAND_SUCCESS;
        data = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, data, 0, data.length);
    }

    public boolean isError() {
        return error;
    }

}
