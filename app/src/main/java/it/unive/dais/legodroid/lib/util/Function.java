package it.unive.dais.legodroid.lib.util;


/**
 * Interfaccia che rappresenta una funzione unaria il cui dominio (tipo di input) è T ed il cui codominio (tipo di ritorno) è R.
 * Questa interfaccia è compatibile con l'interfaccia java.lang.Function di Java 8, tuttavia funziona anche con versioni più vecchie di Java.
 * @param <T> il tipo di input della funzione.
 * @param <R> il tipo di ritorno della funzione.
 * @author Alvise Spanò, Università Ca' Foscari
 */
@FunctionalInterface
public interface Function<T, R>{
    /**
     * Unico metodo dell'interfaccia.
     * Esso implementa il corpo funzione, prendendo un parametro di tipo T e ritornando un risultato di tipo R.
     * @param x parametro di input di tipo T.
     * @return risultato in output di tipo R.
     */
    R apply(T x);

    static <T> Function<T, T> identity() { return x -> x; }
}
