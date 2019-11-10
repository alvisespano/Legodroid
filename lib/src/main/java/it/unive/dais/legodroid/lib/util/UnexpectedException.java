package it.unive.dais.legodroid.lib.util;


import androidx.annotation.NonNull;

/**
 * Unchecked exception representing an unexpected error.
 */
public class UnexpectedException extends RuntimeException {
    /**
     * Constructor by string.
     *
     * @param s the message.
     */
    public UnexpectedException(@NonNull String s) {
        super(s);
    }

    /**
     * Constructor by string and inner throwable.
     *
     * @param s     the message.
     * @param cause the throwable object that caused the error.
     */
    public UnexpectedException(@NonNull String s, @NonNull Throwable cause) {
        super(s, cause);
    }
}
