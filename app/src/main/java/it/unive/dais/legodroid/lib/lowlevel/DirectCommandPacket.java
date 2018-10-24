package it.unive.dais.legodroid.lib.lowlevel;

public class DirectCommandPacket {
    private int length;
    private int counter;
    private boolean reply;
    private int reservation1;
    private int reservation2;
    private byte[] data;

    public DirectCommandPacket(int counter, boolean reply, int localReservation,
                               int globalReservation, byte[] bytecode) {
        if (globalReservation > 1024)
            throw new IllegalArgumentException("Global buffer must be less than 1024 bytes");
        if (localReservation > 64)
            throw new IllegalArgumentException("Local buffer must be less than 64 bytes");

        this.length = bytecode.length + 5;
        this.counter = counter;
        this.reply = reply;
        this.reservation1 = (localReservation << 2) & 0b11111100;
        this.reservation1 |= ((globalReservation >> 8) & 0x03);
        this.reservation2 = globalReservation & 0xFF;
        this.data = bytecode;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[this.length];

        bytes[0] = (byte) (this.length & 0xFF);
        bytes[1] = (byte) ((this.length >> 8) & 0xFF);
        bytes[2] = (byte) (this.counter & 0xFF);
        bytes[3] = (byte) ((this.counter >> 8) & 0xFF);
        bytes[4] = reply ? Constants.DIRECT_COMMAND_REPLY : Constants.DIRECT_COMMAND_NOREPLY;
        bytes[5] = (byte) (this.reservation1 & 0xFF);
        bytes[6] = (byte) (this.reservation2 & 0xFF);
        System.arraycopy(this.data, 0, bytes, 7, data.length);

        return bytes;
    }
}
