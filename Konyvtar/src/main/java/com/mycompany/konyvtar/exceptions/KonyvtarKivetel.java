package com.mycompany.konyvtar.exceptions;

/**
 * Common parent of every library business error.
 * Checked exception: these are expected situations (no free copy, unknown
 * reader...), so the compiler forces the caller - the console menu - to handle them.
 */
public class KonyvtarKivetel extends Exception {
    public KonyvtarKivetel(String uzenet) { super(uzenet); }
}