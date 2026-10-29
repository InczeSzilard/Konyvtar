package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.modell.Konyvtar;

import java.io.IOException;
import java.nio.file.Path;

/*
 * Saves and loads the whole library to / from a file.
 * Two implementations: Java serialization and own CSV format. The rest of the
 * program only knows this interface, so the format can be swapped in main
 * without touching anything else (Dependency Inversion, Liskov substitution).
 */
public interface KonyvtarTar {

    void ment(Konyvtar k, Path fajl) throws IOException;

    /**
     * @throws java.nio.file.NoSuchFileException if the file doesn't exist
     * @throws com.mycompany.konyvtarirendszer.kivetel.SerultAdatfajlKivetel if it exists but is corrupt
     */
    Konyvtar betolt(Path fajl) throws IOException;
}