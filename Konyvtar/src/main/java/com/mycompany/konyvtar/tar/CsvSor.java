package com.mycompany.konyvtar.tar;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a list of fields into one CSV line and back.
 * Separator is ';'. A field containing ';' or '"' is put in quotes, and a '"'
 * inside it is doubled - the usual CSV rule, so Excel can open the file too.
 *   Example:  Rómeó és Júlia; avagy "a szerelem"  ->  "Rómeó és Júlia; avagy ""a szerelem"""
 */
final class CsvSor {

    static final char ELVALASZTO = ';';
    private static final char IDEZOJEL = '"';

    private CsvSor() {
    }

    static String osszefuz(String... mezok) {
        StringBuilder sor = new StringBuilder();
        for (int i = 0; i < mezok.length; i++) {
            if (i > 0) {
                sor.append(ELVALASZTO);
            }
            sor.append(kodol(mezok[i]));
        }
        return sor.toString();
    }

    private static String kodol(String mezo) {
        if (mezo.indexOf('\n') >= 0 || mezo.indexOf('\r') >= 0) {
            // one record = one line, so the line number in an error message stays meaningful
            throw new IllegalArgumentException("Sortores nem lehet egy mezoben: " + mezo);
        }
        if (mezo.indexOf(ELVALASZTO) >= 0 || mezo.indexOf(IDEZOJEL) >= 0) {
            return IDEZOJEL + mezo.replace("\"", "\"\"") + IDEZOJEL;
        }
        return mezo;
    }

    /** Splits one line into fields. An unclosed quote -> IllegalArgumentException. */
    static List<String> szetbont(String sor) {
        List<String> mezok = new ArrayList<>();
        StringBuilder aktualis = new StringBuilder();
        boolean idezetben = false;
        for (int i = 0; i < sor.length(); i++) {
            char c = sor.charAt(i);
            if (idezetben) {
                if (c == IDEZOJEL) {
                    if (i + 1 < sor.length() && sor.charAt(i + 1) == IDEZOJEL) {
                        aktualis.append(IDEZOJEL); // "" inside quotes = one "
                        i++;
                    } else {
                        idezetben = false;         // closing quote
                    }
                } else {
                    aktualis.append(c);
                }
            } else if (c == IDEZOJEL && aktualis.length() == 0) {
                idezetben = true;                  // opening quote at the start of a field
            } else if (c == ELVALASZTO) {
                mezok.add(aktualis.toString());
                aktualis.setLength(0);
            } else {
                aktualis.append(c);
            }
        }
        if (idezetben) {
            throw new IllegalArgumentException("lezaratlan idezojel");
        }
        mezok.add(aktualis.toString());
        return mezok;
    }
}