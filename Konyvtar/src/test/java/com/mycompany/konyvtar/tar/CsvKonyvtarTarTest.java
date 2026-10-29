package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import com.mycompany.konyvtar.modell.Konyvtar;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the whole contract for the CSV storage, plus CSV-specific checks. */
class CsvKonyvtarTarTest extends KonyvtarTarSzerzodesTest {

    @Override
    KonyvtarTar tar() {
        return new CsvKonyvtarTar();
    }

    @Override
    String fajlNev() {
        return "konyvtar.csv";
    }

    @Test
    void pontosvesszoEsIdezojelACimben_helyesenKodolva() throws Exception {
        Path fajl = mappa.resolve(fajlNev());

        tar().ment(TesztAdatok.mintaKonyvtar(), fajl);

        List<String> sorok = Files.readAllLines(fajl, StandardCharsets.UTF_8);
        assertTrue(sorok.contains(
                "KONYV;978-2;\"Romeo es Julia; avagy \"\"a szerelem\"\"\";Shakespeare, William;1597;1"));
    }

    @Test
    void hibasSor_aHibauzenetMegadjaASorSzamot() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        Files.write(fajl, List.of(
                "# megjegyzes",
                "KONYV;978-1;Egri csillagok;Gardonyi Geza;1899;3",
                "KONYV;978-2;Abigel;Szabo Magda;ezerkilencszaz;1"), StandardCharsets.UTF_8);

        SerultAdatfajlKivetel e = assertThrows(SerultAdatfajlKivetel.class, () -> tar().betolt(fajl));

        assertEquals(3, e.getSorSzam());
    }

    @Test
    void tobbKolcsonzesMintPeldany_SerultAdatfajlKivetel() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        Files.write(fajl, List.of(
                "KONYV;978-1;Egri csillagok;Gardonyi Geza;1899;1",
                "OLVASO;O-001;Kiss Anna",
                "OLVASO;O-002;Nagy Peter",
                "KOLCSONZES;978-1;O-001;2026-10-01;2026-10-22",
                "KOLCSONZES;978-1;O-002;2026-10-02;2026-10-23"), StandardCharsets.UTF_8);

        SerultAdatfajlKivetel e = assertThrows(SerultAdatfajlKivetel.class, () -> tar().betolt(fajl));

        assertEquals(5, e.getSorSzam());
    }

    @Test
    void sorokSorrendjeNemSzamit() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        Files.write(fajl, List.of(
                "KOLCSONZES;978-1;O-001;2026-10-01;2026-10-22",
                "OLVASO;O-001;Kiss Anna",
                "KONYV;978-1;Egri csillagok;Gardonyi Geza;1899;3"), StandardCharsets.UTF_8);

        Konyvtar k = tar().betolt(fajl);

        assertEquals(1, k.getKolcsonzesek().size());
    }
}