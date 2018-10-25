package it.unive.dais.legodroid.lib.util;

@FunctionalInterface
public interface Function<T, R>{
    R apply(T x);
    static <T> Function<T, T> identity() { return x -> x; }
}
