package it.unive.dais.legodroid.lib.util;

public interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
}
