package it.unive.dais.legodroid.lib.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ResourceProxy<R> {
    @NonNull
    private R resource;

    public ResourceProxy(@NonNull R resource) {
        this.resource = resource;
    }

    @Nullable
    public <T, E extends Throwable> T perform(@NonNull ThrowingFunction<R, T, E> f) throws E {
        before();
        @Nullable final T result;
        try {
            result = f.applyThrows(resource);
        }
        finally {
            after();
        }
        return result;
    }

    protected abstract void before();
    protected abstract void after();
}
