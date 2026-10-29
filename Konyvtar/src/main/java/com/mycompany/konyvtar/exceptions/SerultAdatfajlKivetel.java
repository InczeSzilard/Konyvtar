package com.mycompany.konyvtar.exceptions;

import java.io.IOException;

/*
 * The data file exists but can't be read (broken line, wrong format, cut-off file).
 * Extends IOException, so it fits the "betolt(...) throws IOException" signature of
 * KonyvtarTar - the caller can still show a sensible message instead of crashing.
 */
public class SerultAdatfajlKivetel extends IOException {
    private final String fajlNev;
    private final int sorSzam; // 0 if the line is unknown (e.g. serialized file)

    public SerultAdatfajlKivetel(String fajlNev, int sorSzam, String ok, Throwable eredeti) {
        super("A(z) " + fajlNev + " adatfajl serult"
                + (sorSzam > 0 ? " (" + sorSzam + ". sor)" : "") + ": " + ok, eredeti);
        this.fajlNev = fajlNev;
        this.sorSzam = sorSzam;
    }

    public String getFajlNev() { return fajlNev; }

    public int getSorSzam() { return sorSzam; }
}