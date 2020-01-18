package it.unive.dais.legodroid.lib.util;

import android.util.Log;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
     * @param r the object of type {@link ThrowingRunnable}.
     */
    public static void trap(ThrowingRunnable<? extends Throwable> r) {
        try {
            r.runThrows();
        } catch (Throwable e) {
            Log.e(TAG, String.format("exception trapped: %s", e));
            e.printStackTrace();
        }
    }

    /**
     * Call a ThrowingConsumer with the given argument of type T.
     * @param c the ThrowingConsumer picking an argument of type T.
     * @param x the argument of type T to be applied.
     * @param <T> the type argument of the {@link ThrowingConsumer}.
     */
    public static <T> void trap(ThrowingConsumer<T, ? extends Throwable> c, T x) {
        trap(() -> c.callThrows(x));
    }

    /**
     * Call a ThrowingFunction with the given argument of type A.
     * @param f the ThrowingFunction picking an argument of type A and returning a value of type B.
     * @param x the argument of type T to be applied.
     * @param <A> the type of the argument of the {@link ThrowingFunction}.
     * @param <B> the return type of the {@link ThrowingFunction}.
     */
    public static <A, B> B trap(ThrowingFunction<A, B, ? extends Throwable> f, A x) {
        try {
            return f.applyThrows(x);
        } catch (Throwable e) {
            Log.e(TAG, String.format("exception trapped: %s", e));
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
