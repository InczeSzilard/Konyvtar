package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.modell.Konyv;
import com.mycompany.konyvtar.modell.Konyvtar;
import com.mycompany.konyvtar.modell.Olvaso;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Shared sample data and a "same state" check for the storage tests. */
final class TesztAdatok {

    private TesztAdatok() {
    }

    /** A library with tricky data: accents, ';' and '"' in a title, two loans. */
    static Konyvtar mintaKonyvtar() throws Exception {
        Konyvtar k = new Konyvtar();
        k.konyvHozzaad(new Konyv("978-1", "Egri csillagok", "Gardonyi Geza", 1899, 3));
        k.konyvHozzaad(new Konyv("978-2", "Romeo es Julia; avagy \"a szerelem\"", "Shakespeare, William", 1597, 1));
        k.konyvHozzaad(new Konyv("978-3", "Abigel", "Szabo Magda", 1970, 2));
        k.olvasoHozzaad(new Olvaso("O-001", "Kiss Anna"));
        k.olvasoHozzaad(new Olvaso("O-002", "Nagy Peter"));
        k.kolcsonoz("978-1", "O-001", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 22));
        k.kolcsonoz("978-2", "O-002", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 26));
        return k;
    }

    /*
     * Compares the FULL state. Konyv.equals only looks at the ISBN, so books are
     * compared field by field via toString (which contains every field).
     */
    static void azonosAllapot(Konyvtar vart, Konyvtar kapott) {
        assertEquals(szovegek(vart.getKonyvek()), szovegek(kapott.getKonyvek()), "konyvek");
        assertEquals(szovegek(vart.getOlvasok()), szovegek(kapott.getOlvasok()), "olvasok");
        assertEquals(vart.getKolcsonzesek(), kapott.getKolcsonzesek(), "kolcsonzesek");
        for (Konyv konyv : vart.getKonyvek()) {
            Konyv masik = kapott.getKonyvek().stream()
                    .filter(konyv::equals).findFirst().orElseThrow();
            assertEquals(vart.szabadPeldany(konyv), kapott.szabadPeldany(masik), "szabad peldany: " + konyv.getIsbn());
        }
    }

    private static List<String> szovegek(java.util.Collection<?> elemek) {
        return elemek.stream().map(Object::toString).toList();
    }
}