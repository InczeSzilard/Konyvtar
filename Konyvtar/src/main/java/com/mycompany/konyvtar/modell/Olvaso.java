package com.mycompany.konyvtar.modell;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/*
 * A registered reader. Immutable.
 * Two readers are equal if their reader card number (olvasojegy-szam) is equal -
 * two different people can have the same name, but never the same card.
 */
public final class Olvaso implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String olvasojegySzam;
    private final String nev;

    public Olvaso(String olvasojegySzam, String nev) {
        if (olvasojegySzam == null || olvasojegySzam.isBlank()) {
            throw new IllegalArgumentException("Az olvasojegy-szam nem lehet ures.");
        }
        if (nev == null || nev.isBlank()) {
            throw new IllegalArgumentException("A nev nem lehet ures.");
        }
        this.olvasojegySzam = olvasojegySzam.trim();
        this.nev = nev.trim();
    }

    public String getOlvasojegySzam() { return olvasojegySzam; }

    public String getNev() { return nev; }

    // ---- Object contract: identity = reader card number ----

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Olvaso)) {
            return false;
        }
        Olvaso masik = (Olvaso) o;
        return olvasojegySzam.equals(masik.olvasojegySzam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(olvasojegySzam);
    }

    @Override
    public String toString() {
        return nev + " (" + olvasojegySzam + ")";
    }
}