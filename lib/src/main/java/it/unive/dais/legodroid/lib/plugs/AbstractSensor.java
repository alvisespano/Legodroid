package it.unive.dais.legodroid.lib.plugs;



import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Function;

/**
 * Abstract class for sensors collecting reusable functionalities.
 */
public abstract class AbstractSensor extends Plug<EV3.InputPort> {
    /**
     * This field represents the type of the direct command according to the GenEV3 Development Kit Documentation, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#EV3_COLOR}.
     */
    protected final int type;

    /**
     * Create an instance of this class given an object of type {@link EV3.Api}, and input port and the type constant.
     * This constructor is meant for subclasses specializing a specific sensor.
     *
     * @param api  an object of type {@link EV3.Api}.
     * @param port input port where the sensor is attached to.
     * @param type type constant, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#EV3_COLOR}.
     */
    protected AbstractSensor(@NonNull EV3.Api api, EV3.InputPort port, int type) {
        super(api, port);
        this.type = type;
    }

    /**
     * Send a PCT command and converts the reply by applying the given function.
     *
     * @param mode   mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @param nvalue number of values for the reply.
     * @param f      function object for converting the array of shorts into an object of type {@link T}.
     * @param <T>    generic type that is the generic argument of the resulting future.
     * @return a future hosting an object of type {@link T}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    protected <T> Future<T> getPercent(int mode, int nvalue, @NonNull Function<short[], T> f) throws IOException {
        Future<short[]> r = api.getPercentValue(port.toByte(), type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    /**
     * Send a PCT command with 1 nvalue and converts the reply by applying the given function.
     * Calling this method is like calling {@link #getPercent(int, int, Function)} with parameter nvalue equal to 1.
     *
     * @param mode mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @param f    function object for converting the array of shorts into an object of type {@link T}.
     * @param <T>  generic type that is the generic argument of the resulting future.
     * @return a future hosting an object of type {@link T}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    protected <T> Future<T> getPercent1(int mode, @NonNull Function<Short, T> f) throws IOException {
        return getPercent(mode, 1, (a) -> f.apply(a[0]));
    }

    /**
     * Send a PCT command with 1 nvalue and returns the resulting short.
     *
     * @param mode mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @return a future hosting a 16-bit short.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    protected Future<Short> getPercent1(int mode) throws IOException {
        return getPercent1(mode, Function.identity());
    }

    /**
     * Send a SI command and converts the reply by applying the given function.
     *
     * @param mode   mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @param nvalue number of values for the reply.
     * @param f      function object for converting the array of floats into an object of type {@link T}.
     * @param <T>    generic type that is the generic argument of the resulting future.
     * @return a future hosting an object of type {@link T}.
     * @throws IOException thrown when communication errors occur.
     */
    @SuppressWarnings("SameParameterValue")
    @NonNull
    protected <T> Future<T> getSi(int mode, int nvalue, @NonNull Function<float[], T> f) throws IOException {
        Future<float[]> r = api.getSiValue(port.toByte(), type, mode, nvalue);
        return api.execAsync(() -> f.apply(r.get()));
    }

    /**
     * Send a SI command with 1 nvalue and converts the reply by applying the given function.
     * Calling this method is like calling {@link #getPercent(int, int, Function)} with parameter nvalue equal to 1.
     *
     * @param mode mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @param f    function object for converting the array of shorts into an object of type {@link T}.
     * @param <T>  generic type that is the generic argument of the resulting future.
     * @return a future hosting an object of type {@link T}.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    protected <T> Future<T> getSi1(int mode, @NonNull Function<Float, T> f) throws IOException {
        return getSi(mode, 1, (a) -> f.apply(a[0]));
    }

    /**
     * Send a PCT command with 1 nvalue and returns the resulting float.
     *
     * @param mode mode constant as defined in {@link it.unive.dais.legodroid.lib.comm.Const}, e.g. {@link it.unive.dais.legodroid.lib.comm.Const#GYRO_ANGLE}.
     * @return a future hosting a 32-bit float.
     * @throws IOException thrown when communication errors occur.
     */
    @NonNull
    protected Future<Float> getSi1(int mode) throws IOException {
        return getSi1(mode, Function.identity());
    }
}
