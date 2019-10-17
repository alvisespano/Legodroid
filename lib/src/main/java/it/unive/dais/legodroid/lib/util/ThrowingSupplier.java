package it.unive.dais.legodroid.lib.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> extends Supplier<T> {
    /**
     * Invoke {@link #getThrows()} and turn possible checked exceptions into an unchecked {@link RuntimeException}.
     */
    @Override
    default T get() {
        try {
            return getThrows();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the function.
     * @throws E the exception.
     */
    T getThrows() throws E;
}
