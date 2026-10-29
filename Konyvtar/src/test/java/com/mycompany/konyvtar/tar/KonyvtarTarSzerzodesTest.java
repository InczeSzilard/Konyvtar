package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import com.mycompany.konyvtar.modell.Konyv;
import com.mycompany.konyvtar.modell.Konyvtar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Contract test: the rules EVERY KonyvtarTar implementation must follow.
 * Each storage gets its own small subclass that only says which storage to test,
 * so both run exactly the same tests (Liskov substitution, checked automatically).
 */
abstract class KonyvtarTarSzerzodesTest {

    @TempDir
    Path mappa; // a fresh empty folder for every test, deleted afterwards

    /** The storage under test. */
    abstract KonyvtarTar tar();

    /** File name with the extension that suits the storage. */
    abstract String fajlNev();

    @Test
    void mentesUtanBetoltve_ugyanazAzAllapot() throws Exception {
        Konyvtar eredeti = TesztAdatok.mintaKonyvtar();
        Path fajl = mappa.resolve(fajlNev());

        tar().ment(eredeti, fajl);
        Konyvtar betoltott = tar().betolt(fajl);

        TesztAdatok.azonosAllapot(eredeti, betoltott);
    }

    @Test
    void uresKonyvtar_mentheto_esBetoltheto() throws Exception {
        Path fajl = mappa.resolve(fajlNev());

        tar().ment(new Konyvtar(), fajl);
        Konyvtar betoltott = tar().betolt(fajl);

        assertTrue(betoltott.getKonyvek().isEmpty());
        assertTrue(betoltott.getKolcsonzesek().isEmpty());
    }

    @Test
    void ujraMentes_felulirja_esNemMaradIdeiglenesFajl() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        Konyvtar konyvtar = TesztAdatok.mintaKonyvtar();
        tar().ment(konyvtar, fajl);

        konyvtar.konyvHozzaad(new Konyv("978-9", "Uj konyv", "Uj Szerzo", 2024, 1));
        tar().ment(konyvtar, fajl);

        assertEquals(4, tar().betolt(fajl).getKonyvek().size());
        try (Stream<Path> fajlok = Files.list(mappa)) {
            assertEquals(List.of(fajl.getFileName()), fajlok.map(Path::getFileName).toList());
        }
    }

    @Test
    void nemLetezoFajl_NoSuchFileException() {
        assertThrows(NoSuchFileException.class, () -> tar().betolt(mappa.resolve(fajlNev())));
    }

    @Test
    void szemetFajl_SerultAdatfajlKivetel_nemOsszeomlas() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        Files.writeString(fajl, "ez nem konyvtari adat");

        SerultAdatfajlKivetel e = assertThrows(SerultAdatfajlKivetel.class, () -> tar().betolt(fajl));

        assertTrue(e.getMessage().contains(fajlNev())); // the message names the file
    }
}