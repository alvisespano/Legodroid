package it.unive.dais.legodroid.lib.sensors;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Function;

public abstract class AbstractSensor {
    protected final int type;
    @NonNull
    protected final EV3.InputPort port;
    @NonNull
    protected final EV3.Api api;

    protected AbstractSensor(@NonNull EV3.Api api, @NonNull EV3.InputPort port, int mode) {
        this.port = port;
        this.api = api;
        this.type = mode;
    }

    protected <T> Future<T> getPercent(int mode, int nvalue, @NonNull Function<short[], T> f) throws IOException {
        Future<short[]> r = api.getPercentValue(port, type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    protected <T> Future<T> getPercent1(int mode, @NonNull Function<Short, T> f) throws IOException {
        return getPercent(mode, 1, (a) -> f.apply(a[0]));
    }

    protected Future<Short> getPercent1(int mode) throws IOException {
        return getPercent1(mode, Function.identity());
    }

    @SuppressWarnings("SameParameterValue")
    protected <T> Future<T> getSi(int mode, int nvalue, @NonNull Function<float[], T> f) throws IOException {
        Future<float[]> r = api.getSiValue(port, type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    protected <T> Future<T> getSi1(int mode, @NonNull Function<Float, T> f) throws IOException {
        return getSi(mode, 1, (a) -> f.apply(a[0]));
    }

    protected Future<Float> getSi1(int mode) throws IOException {
        return getSi1(mode, Function.identity());
    }
}
