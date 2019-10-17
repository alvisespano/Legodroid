package it.unive.dais.legodroid.lib.util;

import java.util.concurrent.ExecutionException;

public interface Future<T> extends java.util.concurrent.Future<T> {
    default void onDone(Consumer<T> f) throws ExecutionException, InterruptedException {
        // TODO: rendere chainabili queste chiamare e decidere se devono essere runnate dal main thread (runOnUI)
        f.call(get());
    }
}
