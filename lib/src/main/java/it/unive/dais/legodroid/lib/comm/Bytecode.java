package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Bytecode {
    private static final byte BYTE_SIZE = (byte) 0b10000001;
    private static final byte SHORT_SIZE = (byte) 0x82;
    private static final byte INT_SIZE = (byte) 0x83;

    @NonNull
    private final ByteArrayOutputStream underlying;
    @NonNull
    private final DataOutputStream out;

    public Bytecode() {
        underlying = new ByteArrayOutputStream();
        out = new DataOutputStream(underlying);
    }

    public void addOpCode(byte opcode) throws IOException {
        out.writeByte(opcode);
    }

    public void addParameter(byte param) throws IOException {
        out.writeByte(BYTE_SIZE);
        out.writeByte(param);
    }

    public void addParameter(short param) throws IOException {
        out.writeByte(SHORT_SIZE);
        out.writeByte(param);
        out.writeByte(param >> 8);
    }

    public void addParameter(int param) throws IOException {
        out.writeByte(INT_SIZE);
        out.writeByte(param);
        out.writeByte(param >> 8);
        out.writeByte(param >> 16);
        out.writeByte(param >> 24);
    }

    public void addGlobalIndex(byte index) throws IOException {
        out.writeByte(index + 0x60);
    }

    public void append(@NonNull Bytecode command) throws IOException {
        out.write(command.getBytes());
    }

    @NonNull
    public byte[] getBytes() {
        return underlying.toByteArray();
    }
}
