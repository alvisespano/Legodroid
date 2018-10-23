package it.unive.dais.legodroid.lib.util;

public class Promise<T> {
    private Consumer<T> resolveHandler;
    private Consumer<T> rejectHandler;
    private T data;

    public Promise() {
        this.resolveHandler = null;
        this.rejectHandler = null;
        this.data = null;
    }

    public void then(Consumer<T> handler) {
        this.resolveHandler = handler;

        if (data != null) {
            handler.call(data);
        }
    }

    public void thenError(Consumer<T> handler) {
        this.rejectHandler = handler;

        if (data != null) {
            handler.call(data);
        }
    }

    public void resolve(T data) {
        this.data = data;

        if (resolveHandler != null) {
            resolveHandler.call(data);
        }
    }

    public void reject(T data) {
        this.data = data;

        if (rejectHandler != null) {
            rejectHandler.call(data);
        }
    }
}
