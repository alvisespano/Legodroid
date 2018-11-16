package it.unive.dais.legodroid.lib.util;

import android.support.annotation.NonNull;

/**
 * Eccezione di tipo inatteso, sottoclasse di RuntimeException.
 * Da usare per segnalare un errore che non è contemplato dall'applicazione, come ad esempio un caso che non dovrebbe accadere oppure scenari
 * non previsti ma che non si possono escludere dal codice del programma.
 * @author Alvise Spanò, Università Ca' Foscari
 */
public class UnexpectedException extends RuntimeException {
    /**
     * Costruttore per stringa.
     * @param s la stringa col messaggio contenuto nell'eccezione.
     */
    public UnexpectedException(@NonNull String s) {
        super(s);
    }

    public UnexpectedException(@NonNull String s, @NonNull Throwable cause) {
        super(s, cause);
    }
}
