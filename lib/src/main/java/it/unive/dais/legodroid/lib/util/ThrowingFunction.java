package it.unive.dais.legodroid.lib.util;

/**
 * Functional interface representing {@link Consumer}'s that can throw exception.
 * @param <T>
 * @param <E>
 */
@FunctionalInterface
public interface ThrowingFunction<A, B, E extends Throwable> extends Function<A, B> {
    /**
     * Invoke {@link #applyThrows} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     * @param x the argument of type T
     */
    @Override
    default B apply(A x) {
        try {
            return applyThrows(x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     * @param x the parameter.
     * @throws E the exception.
     */
    B applyThrows(A x) throws E;
}
