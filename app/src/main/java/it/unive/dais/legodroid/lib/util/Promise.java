package it.unive.dais.legodroid.lib.util;

import android.support.annotation.NonNull;

public class Promise<T> {
    @NonNull
    private Consumer<T> onSuccess;
    @NonNull
    private Consumer<T> onError;
    @NonNull
    private T data;

    public Promise() {
        this.onSuccess = null;
        this.onError = null;
        this.data = null;
    }

    public void onSuccess(Consumer<T> handler) {
        this.onSuccess = handler;

        if (data != null) {
            handler.call(data);
        }
    }

    public void thenError(Consumer<T> handler) {
        this.onError = handler;

        if (data != null) {
            handler.call(data);
        }
    }

    public void resolve(T data) {
        this.data = data;

        if (onSuccess != null) {
            onSuccess.call(data);
        }
    }

    public void reject(T data) {
        this.data = data;

        if (onError != null) {
            onError.call(data);
        }
    }
}
