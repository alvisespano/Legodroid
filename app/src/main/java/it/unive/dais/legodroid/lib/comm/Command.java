package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

public class Command extends Packet {

    private static int sequenceCounter = 0;

    private boolean reply;
    private int reservation1;
    private int reservation2;

    public Command(boolean reply, int localReservation, int globalReservation, @NonNull byte[] bytecode) {
        if (globalReservation > 1024)
            throw new IllegalArgumentException("global buffer must be less than 1024 bytes");
        if (localReservation > 64)
            throw new IllegalArgumentException("local buffer must be less than 64 bytes");

//        this.length = bytecode.length + 5;
        this.counter = Command.sequenceCounter++;
        this.reply = reply;
        this.reservation1 = ((localReservation << 2) & 0b11111100) | ((globalReservation >> 8) & 0x03);
        this.reservation2 = globalReservation & 0xFF;
        this.data = bytecode;
    }

    public byte[] marshal() {
        byte[] bytes = new byte[5 + data.length];
        bytes[0] = (byte) (this.counter & 0xFF);
        bytes[1] = (byte) ((this.counter >> 8) & 0xFF);
        bytes[2] = reply ? Const.DIRECT_COMMAND_REPLY : Const.DIRECT_COMMAND_NOREPLY;
        bytes[3] = (byte) (this.reservation1 & 0xFF);
        bytes[4] = (byte) (this.reservation2 & 0xFF);
        System.arraycopy(this.data, 0, bytes, 5, data.length);
        return bytes;
    }
}
