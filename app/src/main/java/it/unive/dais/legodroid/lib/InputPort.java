package it.unive.dais.legodroid.lib;

import it.unive.dais.legodroid.lib.util.UnexpectedException;

public enum InputPort {
    _1, _2, _3, _4;

    public byte toByte() {
        switch (this) {
            case _1:
                return 0;
            case _2:
                return 1;
            case _3:
                return 2;
            case _4:
                return 3;
        }
        throw new UnexpectedException("invalid input port");
    }
}
