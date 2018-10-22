package it.unive.dais.legodroid.lib.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteCodeGen {
    private static final byte BYTE_SIZE = (byte) 0x81;
    private static final byte SHORT_SIZE = (byte) 0x82;
    private static final byte INT_SIZE = (byte) 0x83;
    private static final byte GLOBAL_INDEX_SIZE = (byte) 0xe1;
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
            mWriter.writeByte(BYTE_SIZE);
            mWriter.writeByte(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addParameter(short param) {
        try {
            mWriter.writeByte(SHORT_SIZE);
            mWriter.writeByte(param);
            mWriter.writeByte(param >> 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addParameter(int param) {
        try {
            mWriter.writeByte(INT_SIZE);
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
            mWriter.writeByte(GLOBAL_INDEX_SIZE);
            mWriter.writeByte(index);
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

        int bodyLength = byteCode.length - 2;

        byteCode[0] = (byte) (bodyLength & 0xff);
        byteCode[1] = (byte) ((bodyLength >>> 8) & 0xff);
        return byteCode;
    }
}
