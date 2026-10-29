package com.mycompany.konyvtar.tar;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/*
 * Crash-safe file writing, shared by both storages:
 *   1. write everything into a temporary file in the SAME folder,
 *   2. force it to the disk,
 *   3. rename it over the real file with ATOMIC_MOVE.
 *
 * The rename is a single step for the operating system: whoever opens the
 * file sees either the complete old version or the complete new one - never
 * a half-written file. If the power goes out during step 1 or 2, only the
 * temporary file is broken; the real data file is untouched.
 */
public final class BiztonsagosMentes {

    /** Writes the content into the given stream (the storage's own format). */
    @FunctionalInterface
    public interface Kiiro {
        void ir(OutputStream ki) throws IOException;
    }

    private BiztonsagosMentes() {
        // utility class, no instances
    }

    public static void ment(Path cel, Kiiro kiiro) throws IOException {
        Path celAbszolut = cel.toAbsolutePath();
        Path mappa = celAbszolut.getParent();
        Files.createDirectories(mappa);

        // Same folder -> same disk, which ATOMIC_MOVE requires.
        Path ideiglenes = Files.createTempFile(mappa, celAbszolut.getFileName().toString() + ".", ".tmp");
        try {
            try (OutputStream fajlKi = Files.newOutputStream(ideiglenes)) {
                kiiro.ir(fajlKi); // the storage may close its own wrapping stream - closing twice is harmless
            }
            try (FileChannel csatorna = FileChannel.open(ideiglenes, StandardOpenOption.WRITE)) {
                csatorna.force(true); // really on the disk, not just in the OS cache
            }
            try {
                Files.move(ideiglenes, celAbszolut, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // very rare (e.g. some network drives): fall back to a normal replace
                Files.move(ideiglenes, celAbszolut, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(ideiglenes); // never leave a broken temp file behind
            throw e;
        }
    }
}