package com.mycompany.konyvtar.modell;

import com.mycompany.konyvtar.exceptions.IsmeretlenKonyvKivetel;
import com.mycompany.konyvtar.exceptions.IsmeretlenOlvasoKivetel;
import com.mycompany.konyvtar.exceptions.NincsIlyenKolcsonzesKivetel;
import com.mycompany.konyvtar.exceptions.NincsSzabadPeldanyKivetel;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/*
 * The library's data: books, readers and active loans - plus the rules that
 * keep this data consistent (no lending without a free copy, no loan for an
 * unknown book or reader).
 *
 * It holds no clock, no fee policy and no file handling: those live in other
 * classes (Single Responsibility). That keeps this class a clean, serializable
 * piece of state that the storages in the next steps can save and load.
 */
public class Konyvtar implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Locale MAGYAR = Locale.forLanguageTag("hu-HU");

    // LinkedHashMap: fast lookup by ISBN / card number AND keeps insertion order
    private final Map<String, Konyv> konyvek = new LinkedHashMap<>();
    private final Map<String, Olvaso> olvasok = new LinkedHashMap<>();
    private final List<Kolcsonzes> kolcsonzesek = new ArrayList<>();

    // ---------- books and readers ----------

    public void konyvHozzaad(Konyv konyv) {
        Objects.requireNonNull(konyv, "konyv");
        if (konyvek.containsKey(konyv.getIsbn())) {
            throw new IllegalArgumentException("Mar van konyv ezzel az ISBN-nel: " + konyv.getIsbn());
        }
        konyvek.put(konyv.getIsbn(), konyv);
    }

    public void olvasoHozzaad(Olvaso olvaso) {
        Objects.requireNonNull(olvaso, "olvaso");
        if (olvasok.containsKey(olvaso.getOlvasojegySzam())) {
            throw new IllegalArgumentException("Mar van olvaso ezzel az olvasojegy-szammal: "
                    + olvaso.getOlvasojegySzam());
        }
        olvasok.put(olvaso.getOlvasojegySzam(), olvaso);
    }

    public Konyv konyv(String isbn) throws IsmeretlenKonyvKivetel {
        Konyv konyv = isbn == null ? null : konyvek.get(isbn.replace(" ", "").trim());
        if (konyv == null) {
            throw new IsmeretlenKonyvKivetel(isbn);
        }
        return konyv;
    }

    public Olvaso olvaso(String olvasojegySzam) throws IsmeretlenOlvasoKivetel {
        Olvaso olvaso = olvasojegySzam == null ? null : olvasok.get(olvasojegySzam.trim());
        if (olvaso == null) {
            throw new IsmeretlenOlvasoKivetel(olvasojegySzam);
        }
        return olvaso;
    }

    // Read-only views: callers can look, but can't bypass the rules by editing the collections.
    public Collection<Konyv> getKonyvek() {
        return Collections.unmodifiableCollection(konyvek.values());
    }

    public Collection<Olvaso> getOlvasok() {
        return Collections.unmodifiableCollection(olvasok.values());
    }

    public List<Kolcsonzes> getKolcsonzesek() {
        return Collections.unmodifiableList(kolcsonzesek);
    }

    // ---------- search and sorting ----------

    /** Books whose title OR author contains the fragment, ignoring upper/lower case. */
    public List<Konyv> keres(String reszlet) {
        String keresett = reszlet == null ? "" : reszlet.trim().toLowerCase(MAGYAR);
        return konyvek.values().stream()
                .filter(k -> k.getCim().toLowerCase(MAGYAR).contains(keresett)
                        || k.getSzerzo().toLowerCase(MAGYAR).contains(keresett))
                .sorted(Konyv.CIM_SZERINT)
                .toList();
    }

    /** All books in the order given by the comparator (e.g. Konyv.CIM_SZERINT, Konyv.EV_SZERINT). */
    public List<Konyv> rendezve(Comparator<Konyv> rendezes) {
        return konyvek.values().stream().sorted(rendezes).toList();
    }

    // ---------- loans ----------

    /** Copies not currently borrowed. */
    public int szabadPeldany(Konyv konyv) {
        long kint = kolcsonzesek.stream().filter(k -> k.konyv().equals(konyv)).count();
        return konyv.getPeldanyszam() - (int) kint;
    }

    /**
     * Lends a book. Checks everything first and only then records the loan,
     * so a refused request leaves the library unchanged.
     */
    public Kolcsonzes kolcsonoz(String isbn, String olvasojegySzam, LocalDate datum, LocalDate hatarido)
            throws IsmeretlenKonyvKivetel, IsmeretlenOlvasoKivetel, NincsSzabadPeldanyKivetel {
        Konyv konyv = konyv(isbn);
        Olvaso olvaso = olvaso(olvasojegySzam);
        if (szabadPeldany(konyv) <= 0) {
            throw new NincsSzabadPeldanyKivetel(konyv.getIsbn(), konyv.getCim());
        }
        Kolcsonzes kolcsonzes = new Kolcsonzes(konyv, olvaso, datum, hatarido);
        kolcsonzesek.add(kolcsonzes);
        return kolcsonzes;
    }

    /**
     * Takes a book back. Returns the closed loan, so the caller can calculate
     * the late fee from its due date.
     */
    public Kolcsonzes visszahoz(String isbn, String olvasojegySzam)
            throws IsmeretlenKonyvKivetel, IsmeretlenOlvasoKivetel, NincsIlyenKolcsonzesKivetel {
        Konyv konyv = konyv(isbn);
        Olvaso olvaso = olvaso(olvasojegySzam);
        Iterator<Kolcsonzes> it = kolcsonzesek.iterator();
        while (it.hasNext()) {
            Kolcsonzes k = it.next();
            if (k.konyv().equals(konyv) && k.olvaso().equals(olvaso)) {
                it.remove(); // safe removal while iterating
                return k;
            }
        }
        throw new NincsIlyenKolcsonzesKivetel(konyv.getIsbn(), olvaso.getOlvasojegySzam());
    }

    public List<Kolcsonzes> olvasoKolcsonzesei(String olvasojegySzam) throws IsmeretlenOlvasoKivetel {
        Olvaso olvaso = olvaso(olvasojegySzam);
        return kolcsonzesek.stream().filter(k -> k.olvaso().equals(olvaso)).toList();
    }

    public List<Kolcsonzes> lejartKolcsonzesek(LocalDate napon) {
        return kolcsonzesek.stream()
                .filter(k -> k.lejart(napon))
                .sorted(Comparator.comparing(Kolcsonzes::hatarido))
                .toList();
    }

    // ---------- integrity check after deserialization ----------

    /**
     * Called by Java serialization right after reading the fields.
     * A manipulated or mismatched file must not produce an inconsistent library.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        for (Kolcsonzes k : kolcsonzesek) {
            if (!k.konyv().equals(konyvek.get(k.konyv().getIsbn()))
                    || !k.olvaso().equals(olvasok.get(k.olvaso().getOlvasojegySzam()))) {
                throw new InvalidObjectException("Kolcsonzes ismeretlen konyvre vagy olvasora: " + k);
            }
        }
        for (Konyv konyv : konyvek.values()) {
            if (szabadPeldany(konyv) < 0) {
                throw new InvalidObjectException("Tobb kolcsonzes, mint peldany: " + konyv.getIsbn());
            }
        }
    }
}