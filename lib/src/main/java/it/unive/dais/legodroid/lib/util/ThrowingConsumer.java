package it.unive.dais.legodroid.lib.util;

/**
 * Functional interface representing {@link Consumer}'s that can throw exception.
 * @param <T>
 * @param <E>
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {
    /**
     * Invoke {@link #callThrows(Object)} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     * @param elem
     */
    @Override
    default void call(T elem) {
        try {
            callThrows(elem);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     * @param elem the parameter.
     * @throws E the exception.
     */
    void callThrows(T elem) throws E;
}
