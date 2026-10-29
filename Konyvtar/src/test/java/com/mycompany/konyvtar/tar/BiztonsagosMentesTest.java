package com.mycompany.konyvtar.tar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BiztonsagosMentesTest {

    @TempDir
    Path mappa;

    @Test
    void mentesKozbeniHiba_aRegiFajlEpMarad_esNemMaradIdeiglenesFajl() throws IOException {
        Path fajl = mappa.resolve("adat.csv");
        BiztonsagosMentes.ment(fajl, ki -> ki.write("regi, teljes tartalom".getBytes(StandardCharsets.UTF_8)));

        // the writer dies halfway - like a power cut during saving
        assertThrows(IOException.class, () -> BiztonsagosMentes.ment(fajl, ki -> {
            ki.write("felig kiir".getBytes(StandardCharsets.UTF_8));
            throw new IOException("aramszünet");
        }));

        assertEquals("regi, teljes tartalom", Files.readString(fajl, StandardCharsets.UTF_8));
        try (Stream<Path> fajlok = Files.list(mappa)) {
            assertEquals(List.of(fajl.getFileName()), fajlok.map(Path::getFileName).toList());
        }
    }
}