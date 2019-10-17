package it.unive.dais.legodroid.lib.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> extends Function<T, R> {
    /**
     * Invoke {@link #applyThrows(T)} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     */
    @Override
    default R apply(T x) {
        try {
            return applyThrows(x);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     *
     * @throws E the exception.
     */
    R applyThrows(T x) throws E;
}
