package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.Nullable;

public abstract class Packet {
    protected int counter;
    @Nullable
    protected byte[] data = null;

    public int getCounter() {
        return counter;
    }

    public byte[] getData() { return data; }

}
