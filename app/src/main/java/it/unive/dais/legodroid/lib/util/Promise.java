package it.unive.dais.legodroid.lib.util;

public class Promise<T> {
    private Handler<T> resolveHandler;
    private Handler<T> rejectHandler;
    private T data;

    public Promise() {
        this.resolveHandler = null;
        this.rejectHandler = null;
        this.data = null;
    }

    public void then(Handler<T> handler) {
        this.resolveHandler = handler;

        if (data != null) {
            handler.call(data);
        }
    }

    public void thenError(Handler<T> handler) {
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
