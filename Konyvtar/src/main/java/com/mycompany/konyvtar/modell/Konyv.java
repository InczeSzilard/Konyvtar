package com.mycompany.konyvtar.modell;

import java.io.Serial;
import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/**
 * A book in the library catalogue. Immutable: once created it never changes,
 * so it is safe to use as a key in a HashMap / HashSet.
 *
 * Two books are equal if their ISBN is equal - title, author etc. don't matter
 * (the ISBN is the book's identity).
 */
public final class Konyv implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Hungarian alphabetical order ("Á" next to "A", not after "Z"), case-insensitive.
    private static final Collator MAGYAR_ABC = Collator.getInstance(Locale.forLanguageTag("hu-HU"));

    static {
        MAGYAR_ABC.setStrength(Collator.SECONDARY); // ignore upper/lower case
    }

    /** Sort by title (Hungarian alphabet); same title -> by ISBN, so the order is always stable. */
    public static final Comparator<Konyv> CIM_SZERINT =
            Comparator.comparing(Konyv::getCim, MAGYAR_ABC).thenComparing(Konyv::getIsbn);

    /** Sort by publication year; same year -> by title. */
    public static final Comparator<Konyv> EV_SZERINT =
            Comparator.comparingInt(Konyv::getKiadasEve).thenComparing(CIM_SZERINT);

    private final String isbn;
    private final String cim;
    private final String szerzo;
    private final int kiadasEve;
    private final int peldanyszam;

    public Konyv(String isbn, String cim, String szerzo, int kiadasEve, int peldanyszam) {
        this.isbn = nemUres(isbn, "ISBN").replace(" ", "");
        this.cim = nemUres(cim, "cim");
        this.szerzo = nemUres(szerzo, "szerzo");
        if (kiadasEve < 1 || kiadasEve > 9999) {
            throw new IllegalArgumentException("Ervenytelen kiadasi ev: " + kiadasEve);
        }
        if (peldanyszam < 1) {
            throw new IllegalArgumentException("A peldanyszamnak legalabb 1-nek kell lennie: " + peldanyszam);
        }
        this.kiadasEve = kiadasEve;
        this.peldanyszam = peldanyszam;
    }

    private static String nemUres(String ertek, String mezo) {
        if (ertek == null || ertek.isBlank()) {
            throw new IllegalArgumentException("A(z) " + mezo + " nem lehet ures.");
        }
        return ertek.trim();
    }

    public String getIsbn() { return isbn; }

    public String getCim() { return cim; }

    public String getSzerzo() { return szerzo; }

    public int getKiadasEve() { return kiadasEve; }

    /** Total number of copies the library owns (borrowed ones included). */
    public int getPeldanyszam() { return peldanyszam; }

    // ---- Object contract: identity = ISBN ----

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Konyv)) {
            return false;
        }
        Konyv masik = (Konyv) o;
        return isbn.equals(masik.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn); // must use exactly the same field(s) as equals
    }

    @Override
    public String toString() {
        return cim + " – " + szerzo + " (" + kiadasEve + "), ISBN: " + isbn + ", " + peldanyszam + " db";
    }
}