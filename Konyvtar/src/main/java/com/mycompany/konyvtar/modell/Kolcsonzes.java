package com.mycompany.konyvtar.modell;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * One loan: which book, which reader, when it was borrowed and when it is due.
 *
 * A record: immutable, and equals/hashCode/toString are generated from all
 * four components. Book and reader compare by ISBN / card number (their own
 * equals), so two loans are equal if the same reader borrowed the same book
 * on the same day with the same due date.
 */
public record Kolcsonzes(Konyv konyv, Olvaso olvaso, LocalDate datum, LocalDate hatarido)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Compact constructor: also runs when a record is deserialized,
    // so a tampered file can't produce an invalid loan.
    public Kolcsonzes {
        Objects.requireNonNull(konyv, "konyv");
        Objects.requireNonNull(olvaso, "olvaso");
        Objects.requireNonNull(datum, "datum");
        Objects.requireNonNull(hatarido, "hatarido");
        if (hatarido.isBefore(datum)) {
            throw new IllegalArgumentException("A hatarido nem lehet a kolcsonzes napja elott.");
        }
    }

    /** How many days late the book is on the given day (0 if not late yet). */
    public long kesesNapokban(LocalDate napon) {
        return Math.max(0, ChronoUnit.DAYS.between(hatarido, napon));
    }

    public boolean lejart(LocalDate napon) {
        return kesesNapokban(napon) > 0;
    }
}