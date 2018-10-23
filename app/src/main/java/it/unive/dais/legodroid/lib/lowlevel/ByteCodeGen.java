package it.unive.dais.legodroid.lib.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteCodeGen {
    private ByteArrayOutputStream mStream;

    private DataOutputStream mWriter;

    public ByteCodeGen() {
        mStream = new ByteArrayOutputStream();
        mWriter = new DataOutputStream(mStream);
    }

    public void addOpCode(byte opcode) {
        try {
            mWriter.writeByte(opcode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addParameter(byte param) {
        try {
            mWriter.writeByte(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addParameter(short param) {
        try {
            mWriter.writeByte(param);
            mWriter.writeByte(param >> 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addParameter(int param) {
        try {
            mWriter.writeByte(param);
            mWriter.writeByte(param >> 8);
            mWriter.writeByte(param >> 16);
            mWriter.writeByte(param >> 24);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGlobalIndex(byte index) {
        try {
            mWriter.writeByte(index + 0x60);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendCommand(ByteCodeGen command) {
        try {
            mWriter.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes() {
        byte[] byteCode = mStream.toByteArray();
        return byteCode;
    }
}
