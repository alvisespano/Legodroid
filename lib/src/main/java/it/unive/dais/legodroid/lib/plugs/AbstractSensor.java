package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Function;

public abstract class AbstractSensor extends Plug<EV3.InputPort> {
    protected final int type;

    protected AbstractSensor(@NonNull EV3.Api api, EV3.InputPort port, int mode) {
        super(api, port);
        this.type = mode;
    }

    @NonNull
    protected <T> Future<T> getPercent(int mode, int nvalue, @NonNull Function<short[], T> f) throws IOException {
        Future<short[]> r = api.getPercentValue(port.toByte(), type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    @NonNull
    protected <T> Future<T> getPercent1(int mode, @NonNull Function<Short, T> f) throws IOException {
        return getPercent(mode, 1, (a) -> f.apply(a[0]));
    }

    @NonNull
    protected Future<Short> getPercent1(int mode) throws IOException {
        return getPercent1(mode, Function.identity());
    }

    @SuppressWarnings("SameParameterValue")
    @NonNull
    protected <T> Future<T> getSi(int mode, int nvalue, @NonNull Function<float[], T> f) throws IOException {
        Future<float[]> r = api.getSiValue(port.toByte(), type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    @NonNull
    protected <T> Future<T> getSi1(int mode, @NonNull Function<Float, T> f) throws IOException {
        return getSi(mode, 1, (a) -> f.apply(a[0]));
    }

    @NonNull
    protected Future<Float> getSi1(int mode) throws IOException {
        return getSi1(mode, Function.identity());
    }
}
