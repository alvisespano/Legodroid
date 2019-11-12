package it.unive.dais.legodroid.lib.comm;


import androidx.annotation.NonNull;

/**
 * Abstract class that serves as a superclass for {@link Command} and {@link Reply}.
 */
public abstract class Packet {
    /**
     * The sequence number: always increasing and uniquely identifying the packet.
     */
    protected final int counter;
    /**
     * The data attached to the packet as a byte array.
     */
    @NonNull
    protected final byte[] data;

    /**
     * Create a packet given a counter and a byte array of data.
     * This constructor is meant for subclasses.
     *
     * @param counter sequence number.
     * @param data    data byte array.
     */
    protected Packet(int counter, @NonNull byte[] data) {
        this.counter = counter;
        this.data = data;
    }

    /**
     * Get the sequence number.
     *
     * @return the sequence number of this packet.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Get the data.
     *
     * @return the data as a byte array.
     */
    @NonNull
    public byte[] getData() {
        return data;
    }

}
