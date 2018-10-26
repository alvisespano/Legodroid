package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public abstract class Packet {
    protected int length;
    protected int counter;
    protected byte[] data;

    public int getLength() {
        return length;
    }

    public int getCounter() {
        return counter;
    }

}
