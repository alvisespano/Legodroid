package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public class Command extends Packet {

    private static int sequenceCounter = 0;

    private boolean reply;
    private int reservationH;
    private int reservationL;

    public Command(boolean reply, int localReservation, int globalReservation, @NonNull byte[] bytecode) {
        super(Command.sequenceCounter++, bytecode);
        if (globalReservation > 1024)
            throw new IllegalArgumentException("global buffer must be less than 1024 bytes");
        if (localReservation > 64)
            throw new IllegalArgumentException("local buffer must be less than 64 bytes");

        this.reply = reply;
        this.reservationH = ((localReservation << 2) & ~0x3) | ((globalReservation >> 8) & 0x03);
        this.reservationL = globalReservation & 0xFF;
    }

    @NonNull
    public byte[] marshal() {
        byte[] bytes = new byte[5 + getData().length];
        bytes[0] = (byte) (getCounter() & 0xFF);
        bytes[1] = (byte) ((getCounter() >> 8) & 0xFF);
        bytes[2] = reply ? Const.DIRECT_COMMAND_REPLY : Const.DIRECT_COMMAND_NOREPLY;
        bytes[3] = (byte) (this.reservationL & 0xFF);
        bytes[4] = (byte) (this.reservationH & 0xFF);
        System.arraycopy(getData(), 0, bytes, 5, getData().length);
        return bytes;
    }
}
