package it.unive.dais.legodroid.lib.util;

import java.util.function.Consumer;

/**
 * Functional interface representing {@link Consumer}'s that can throw exception.
 * @param <T>
 * @param <E>
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {
    /**
     * Invoke {@link #acceptThrows(Object)} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     * @param elem
     */
    @Override
    default void accept(T elem) {
        try {
            acceptThrows(elem);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     * @param elem the parameter.
     * @throws E the exception.
     */
    void acceptThrows(T elem) throws E;
}
