package it.unive.dais.legodroid.lib.util;

/**
 * Functional interface that represents {@link Runnable}'s that can throw exceptions.
 * @param <E>
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> extends Runnable {
    /**
     * Invoke {@link #runThrows} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     */
    @Override
    default void run() {
        try {
            runThrows();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     * @throws E the exception.
     */

    void runThrows() throws E;
}
