package com.mycompany.konyvtar.service;

import com.mycompany.konyvtar.exceptions.KonyvtarKivetel;
import com.mycompany.konyvtar.modell.Kolcsonzes;
import com.mycompany.konyvtar.modell.Konyvtar;
import com.mycompany.konyvtar.rule.KesedelmiDijSzamito;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/*
 * Borrowing and returning "today": knows the current date, the loan period
 * and the late-fee policy - the things Konyvtar deliberately doesn't know.
 *
 * Stateless with respect to the library: every method receives the Konyvtar
 * to work on, so after loading a new Konyvtar from a file nothing has to be
 * re-wired.
 */
public class KolcsonzesiSzolgaltatas {

    private final Clock ora;
    private final int kolcsonzesiNapok;
    private final KesedelmiDijSzamito dijSzamito;

    /**
     * @param ora              injected clock (tests use a fixed one)
     * @param kolcsonzesiNapok loan period in days, e.g. 21
     * @param dijSzamito       late-fee policy (Strategy)
     */
    public KolcsonzesiSzolgaltatas(Clock ora, int kolcsonzesiNapok, KesedelmiDijSzamito dijSzamito) {
        if (kolcsonzesiNapok < 1) {
            throw new IllegalArgumentException("A kolcsonzesi idonek legalabb 1 napnak kell lennie.");
        }
        this.ora = Objects.requireNonNull(ora, "ora");
        this.kolcsonzesiNapok = kolcsonzesiNapok;
        this.dijSzamito = Objects.requireNonNull(dijSzamito, "dijSzamito");
    }

    public LocalDate ma() {
        return LocalDate.now(ora);
    }

    /** Lends the book from today; the due date is today + loan period. */
    public Kolcsonzes kolcsonoz(Konyvtar konyvtar, String isbn, String olvasojegySzam) throws KonyvtarKivetel {
        LocalDate ma = ma();
        return konyvtar.kolcsonoz(isbn, olvasojegySzam, ma, ma.plusDays(kolcsonzesiNapok));
    }

    /** Takes the book back today and calculates the late fee. */
    public Visszahozas visszahoz(Konyvtar konyvtar, String isbn, String olvasojegySzam) throws KonyvtarKivetel {
        Kolcsonzes kolcsonzes = konyvtar.visszahoz(isbn, olvasojegySzam);
        LocalDate ma = ma();
        return new Visszahozas(kolcsonzes, ma, kolcsonzes.kesesNapokban(ma), dijSzamito.dij(kolcsonzes, ma));
    }

    /** Fee the reader would pay if the book came back today (for listings). */
    public long mostaniDij(Kolcsonzes kolcsonzes) {
        return dijSzamito.dij(kolcsonzes, ma());
    }
}