package it.unive.dais.legodroid.lib.util;

@FunctionalInterface
public interface Consumer<T> {
    void call(T data);
}
