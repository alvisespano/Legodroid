package it.unive.dais.legodroid.lib.comm;


import androidx.annotation.NonNull;

/**
 * This class implements a command for the EV3 device.
 */
public class Command extends Packet {

    private static int sequenceCounter = 0;

    private boolean hasReply;
    private int reservationH;
    private int reservationL;

    /**
     * Create a command given some parameters.
     *
     * @param hasReply          true if the commands expects a reply; false otherwise.
     * @param localReservation  local reservation in bytes.
     * @param globalReservation global reservation in bytes.
     * @param bytecode          the bytecode of the command.
     */
    public Command(boolean hasReply, int localReservation, int globalReservation, @NonNull byte[] bytecode) {
        super(Command.sequenceCounter++, bytecode);
        if (globalReservation > 1024)
            throw new IllegalArgumentException("global buffer must be less than 1024 bytes");
        if (localReservation > 64)
            throw new IllegalArgumentException("local buffer must be less than 64 bytes");

        this.hasReply = hasReply;
        this.reservationH = ((localReservation << 2) & ~0x3) | ((globalReservation >> 8) & 0x03);
        this.reservationL = globalReservation & 0xFF;
    }

    /**
     * Serialized the command into an array of bytes.
     *
     * @return the array of byte with the raw representation of the command.
     */
    @NonNull
    public byte[] marshal() {
        byte[] bytes = new byte[5 + getData().length];
        bytes[0] = (byte) (getCounter() & 0xFF);
        bytes[1] = (byte) ((getCounter() >> 8) & 0xFF);
        bytes[2] = hasReply ? Const.DIRECT_COMMAND_REPLY : Const.DIRECT_COMMAND_NOREPLY;
        bytes[3] = (byte) (this.reservationL & 0xFF);
        bytes[4] = (byte) (this.reservationH & 0xFF);
        System.arraycopy(getData(), 0, bytes, 5, getData().length);
        return bytes;
    }

}
