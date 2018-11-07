package it.unive.dais.legodroid.lib;

import it.unive.dais.legodroid.lib.util.UnexpectedException;

public enum OutputPort {
    A, B, C, D;

    public byte toByte() {
        switch (this) {
            case A:
                return 0;
            case B:
                return 1;
            case C:
                return 2;
            case D:
                return 3;
        }
        throw new UnexpectedException("invalid output port");
    }
}
