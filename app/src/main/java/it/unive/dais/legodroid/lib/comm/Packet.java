package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public abstract class Packet {
    protected int counter;
    protected byte[] data;

    public int getCounter() {
        return counter;
    }

    public byte[] getData() { return data; }

}
