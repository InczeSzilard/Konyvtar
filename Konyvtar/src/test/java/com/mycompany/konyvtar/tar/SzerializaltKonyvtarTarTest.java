package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the whole contract for the serialization storage, plus a truncated-file check. */
class SzerializaltKonyvtarTarTest extends KonyvtarTarSzerzodesTest {

    @Override
    KonyvtarTar tar() {
        return new SzerializaltKonyvtarTar();
    }

    @Override
    String fajlNev() {
        return "konyvtar.ser";
    }

    @Test
    void felbevagottFajl_SerultAdatfajlKivetel() throws Exception {
        Path fajl = mappa.resolve(fajlNev());
        tar().ment(TesztAdatok.mintaKonyvtar(), fajl);
        byte[] tartalom = Files.readAllBytes(fajl);
        Files.write(fajl, Arrays.copyOf(tartalom, tartalom.length / 2)); // as if the power went out

        assertThrows(SerultAdatfajlKivetel.class, () -> tar().betolt(fajl));
    }
}