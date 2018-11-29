package it.unive.dais.legodroid.lib.util;

/**
 * Functional interface that represents a function with a parameter of type {@link T} and a result type of type {@link R}.
 *
 * @param <T> the type of the parameter.
 * @param <R> the type of the result type.
 */
@FunctionalInterface
public interface Function<T, R> {
    /**
     * Call the function.
     * @param x the parameter.
     * @return the result.
     */
    R apply(T x);

    /**
     * The generic identity function.
     * @param <T> the type of both the parameter and the result type.
     * @return the result.
     */
    static <T> Function<T, T> identity() {
        return x -> x;
    }
}
