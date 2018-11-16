package it.unive.dais.legodroid.lib.util;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {
    @Override
    default void call(T elem) {
        try {
            callThrows(elem);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void callThrows(T elem) throws E;
}
