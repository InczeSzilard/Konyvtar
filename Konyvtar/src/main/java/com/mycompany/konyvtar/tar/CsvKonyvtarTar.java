package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.exceptions.KonyvtarKivetel;
import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import com.mycompany.konyvtar.modell.Kolcsonzes;
import com.mycompany.konyvtar.modell.Konyv;
import com.mycompany.konyvtar.modell.Konyvtar;
import com.mycompany.konyvtar.modell.Olvaso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Storage #2: own, human-readable CSV format (UTF-8, ';' separated).
 * One record per line; the first field says what the line is:
 *
 *   # comment lines and empty lines are ignored
 *   KONYV;isbn;cim;szerzo;kiadas eve;peldanyszam
 *   OLVASO;olvasojegy-szam;nev
 *   KOLCSONZES;isbn;olvasojegy-szam;datum (EEEE-HH-NN);hatarido (EEEE-HH-NN)
 *
 * Any broken line stops the loading with a SerultAdatfajlKivetel that names
 * the file and the line number.
 */
public class CsvKonyvtarTar implements KonyvtarTar {

    static final String KONYV = "KONYV";
    static final String OLVASO = "OLVASO";
    static final String KOLCSONZES = "KOLCSONZES";

    // ---------------------------------------------------------------- saving

    @Override
    public void ment(Konyvtar konyvtar, Path fajl) throws IOException {
        Objects.requireNonNull(konyvtar, "konyvtar");
        BiztonsagosMentes.ment(fajl, ki -> {
            try (BufferedWriter iro = new BufferedWriter(new OutputStreamWriter(ki, StandardCharsets.UTF_8))) {
                iro.write("# KonyvtariRendszer adatfajl (UTF-8, ';' elvalaszto)");
                iro.newLine();
                for (Konyv k : konyvtar.getKonyvek()) {
                    sort(iro, KONYV, k.getIsbn(), k.getCim(), k.getSzerzo(),
                            String.valueOf(k.getKiadasEve()), String.valueOf(k.getPeldanyszam()));
                }
                for (Olvaso o : konyvtar.getOlvasok()) {
                    sort(iro, OLVASO, o.getOlvasojegySzam(), o.getNev());
                }
                for (Kolcsonzes k : konyvtar.getKolcsonzesek()) {
                    sort(iro, KOLCSONZES, k.konyv().getIsbn(), k.olvaso().getOlvasojegySzam(),
                            k.datum().toString(), k.hatarido().toString());
                }
            }
        });
    }

    private static void sort(BufferedWriter iro, String... mezok) throws IOException {
        iro.write(CsvSor.osszefuz(mezok));
        iro.newLine();
    }

    // ---------------------------------------------------------------- loading

    /** A loan line kept for the second pass, with its line number for error messages. */
    private record FuggoKolcsonzes(int sorSzam, List<String> mezok) {
    }

    @Override
    public Konyvtar betolt(Path fajl) throws IOException {
        String nev = fajl.getFileName().toString();
        Konyvtar konyvtar = new Konyvtar();
        List<FuggoKolcsonzes> kolcsonzesek = new ArrayList<>();

        // Pass 1: books and readers; loans are only collected
        try (BufferedReader olvaso = Files.newBufferedReader(fajl, StandardCharsets.UTF_8)) {
            String sor;
            int sorSzam = 0;
            while ((sor = olvaso.readLine()) != null) {
                sorSzam++;
                if (sor.isBlank() || sor.startsWith("#")) {
                    continue;
                }
                try {
                    List<String> mezok = CsvSor.szetbont(sor);
                    switch (mezok.get(0)) {
                        case KONYV -> {
                            mezoSzam(mezok, 6);
                            konyvtar.konyvHozzaad(new Konyv(mezok.get(1), mezok.get(2), mezok.get(3),
                                    Integer.parseInt(mezok.get(4).trim()), Integer.parseInt(mezok.get(5).trim())));
                        }
                        case OLVASO -> {
                            mezoSzam(mezok, 3);
                            konyvtar.olvasoHozzaad(new Olvaso(mezok.get(1), mezok.get(2)));
                        }
                        case KOLCSONZES -> {
                            mezoSzam(mezok, 5);
                            kolcsonzesek.add(new FuggoKolcsonzes(sorSzam, mezok));
                        }
                        default -> throw new IllegalArgumentException("ismeretlen sortipus: \"" + mezok.get(0) + "\"");
                    }
                } catch (NumberFormatException e) {
                    throw new SerultAdatfajlKivetel(nev, sorSzam, "hibas szam: " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    throw new SerultAdatfajlKivetel(nev, sorSzam, e.getMessage(), e);
                }
            }
        } catch (CharacterCodingException e) {
            throw new SerultAdatfajlKivetel(nev, 0, "nem UTF-8 szovegfajl (talan szerializalt fajl?)", e);
        }

        // Pass 2: loans - now every book and reader is known, whatever their order in the file
        for (FuggoKolcsonzes f : kolcsonzesek) {
            List<String> m = f.mezok();
            try {
                konyvtar.kolcsonoz(m.get(1), m.get(2), LocalDate.parse(m.get(3).trim()), LocalDate.parse(m.get(4).trim()));
            } catch (DateTimeParseException e) {
                throw new SerultAdatfajlKivetel(nev, f.sorSzam(), "hibas datum: " + e.getParsedString(), e);
            } catch (KonyvtarKivetel | IllegalArgumentException e) {
                // unknown book/reader, more loans than copies, due date before loan date
                throw new SerultAdatfajlKivetel(nev, f.sorSzam(), e.getMessage(), e);
            }
        }
        return konyvtar;
    }

    private static void mezoSzam(List<String> mezok, int vart) {
        if (mezok.size() != vart) {
            throw new IllegalArgumentException(mezok.get(0) + " sorban " + vart + " mezo kell, de "
                    + mezok.size() + " van");
        }
    }
}