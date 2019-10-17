package it.unive.dais.legodroid.lib.util;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * This class provides miscellaneous utilities.
 */
public class Prelude {
    private static final String TAG_BASE = "Legodroid";
    public static final String TAG = ReTAG("Prelude");

    public static String ReTAG(String tag) {
        return String.format("%s.%s", TAG_BASE, tag);
    }

    public static String ReTAG(String parent, String tag) {
        return ReTAG(String.format("%s.%s", parent, tag));
    }

    @NonNull
    public static byte[] concat(@NonNull byte[] first, @NonNull byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    @NonNull
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Call a runnable that can throw an exception and trap the invocation surrounding it with a try-catch block.
     *
     * @param f the object of type {@link ThrowingRunnable}.
     */
    public static void trap(ThrowingRunnable<? extends Throwable> f) {
        trap(() -> {
            f.run();
            return null;
        });
    }

    /**
     * Call a ThrowingConsumer with the given argument of type T.
     *
     * @param f   the ThrowingConsumer picking an argument of type T.
     * @param x   the argument of type T to be applied.
     * @param <T> then type of the argument.
     */
    public static <T> void trap(ThrowingConsumer<T, ? extends Throwable> f, T x) {
        trap(() -> f.accept(x));
    }

    /**
     * Call a ThrowingSupplier with the given argument of type T.
     *
     * @param f   the ThrowingConsumer picking an argument of type T.
     * @param <T> the result type.
     */
    public static <T> T trap(ThrowingSupplier<T, ? extends Throwable> f) {
        return f.get();
    }

    /**
     * Call a ThrowingFunction with the given argument of type T.
     *
     * @param f   the ThrowingConsumer picking an argument of type T.
     * @param x   the argument of type T to be applied.
     * @param <T> the type of the argument.
     * @param <R> the result type.
     */
    public static <T, R> R trap(ThrowingFunction<T, R, ? extends Throwable> f, T x) {
        return f.apply(x);
    }

}
