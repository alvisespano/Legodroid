package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class is a low-level buffer for creating commands to be sent to the GenEV3 device.
 * Parts can be appended using this class methods according to which data type and command segment is needed.
 *
 * @see <a href="http://google.com</a>https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">GenEV3 Developer Kit Documentation</a>
 */
public class Bytecode {
    private static final byte BYTE_SIZE = (byte) 0b10000001;
    private static final byte SHORT_SIZE = (byte) 0x82;
    private static final byte INT_SIZE = (byte) 0x83;

    @NonNull
    private final ByteArrayOutputStream underlying;
    @NonNull
    private final DataOutputStream out;

    /**
     * Create an empty object.
     */
    public Bytecode() {
        underlying = new ByteArrayOutputStream();
        out = new DataOutputStream(underlying);
    }

    /**
     * Append the give op-code.
     *
     * @param opcode the op-code as a byte.
     * @throws IOException thrown when communication errors occur.
     * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
     */
    public void addOpCode(byte opcode) throws IOException {
        out.writeByte(opcode);
    }

    /**
     * Append the give parameter.
     *
     * @param param the parameter as a 8-bit byte.
     * @throws IOException thrown when communication errors occur.
     * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
     */
    public void addParameter(byte param) throws IOException {
        out.writeByte(BYTE_SIZE);
        out.writeByte(param);
    }

    /**
     * Append the give parameter.
     *
     * @param param the parameter as a 16-bit short integer.
     * @throws IOException thrown when communication errors occur.
     * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
     */
    public void addParameter(short param) throws IOException {
        out.writeByte(SHORT_SIZE);
        out.writeByte(param);
        out.writeByte(param >> 8);
    }

    /**
     * Append the give parameter.
     *
     * @param param the parameter as a 32-bit integer.
     * @throws IOException thrown when communication errors occur.
     * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
     */
    public void addParameter(int param) throws IOException {
        out.writeByte(INT_SIZE);
        out.writeByte(param);
        out.writeByte(param >> 8);
        out.writeByte(param >> 16);
        out.writeByte(param >> 24);
    }

    /**
     * Append the global index part.
     *
     * @param index the index as a 8-bit byte.
     * @throws IOException thrown when communication errors occur.
     * @see <a href="https://le-www-live-s.legocdn.com/sc/media/files/ev3-developer-kit/lego%20mindstorms%20ev3%20firmware%20developer%20kit-7be073548547d99f7df59ddfd57c0088.pdf?la=en-us">LEGO Mindstorms GenEV3 Firmware Developer Kit</a>
     */
    public void addGlobalIndex(byte index) throws IOException {
        out.writeByte(index + 0x60);
    }

    /**
     * Append another object of type {@link Bytecode}.
     *
     * @param bc the bytecode to be appended.
     * @throws IOException thrown when communication errors occur.
     */
    public void append(@NonNull Bytecode bc) throws IOException {
        out.write(bc.getBytes());
    }

    /**
     * Get the bytecode as a byte array.
     *
     * @return the byte array representing this bytecode.
     */
    @NonNull
    public byte[] getBytes() {
        return underlying.toByteArray();
    }
}
