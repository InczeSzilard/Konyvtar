package com.mycompany.konyvtar.exceptions;

/** No reader with this reader card number is registered. */
public class IsmeretlenOlvasoKivetel extends KonyvtarKivetel {
    private final String olvasojegySzam;

    public IsmeretlenOlvasoKivetel(String olvasojegySzam) {
        super("Nincs ilyen olvasojegy-szamu olvaso: " + olvasojegySzam);
        this.olvasojegySzam = olvasojegySzam;
    }

    public String getOlvasojegySzam() { return olvasojegySzam; }
}