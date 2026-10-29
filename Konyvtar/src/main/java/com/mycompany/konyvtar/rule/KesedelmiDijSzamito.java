package com.mycompany.konyvtar.rule;

import com.mycompany.konyvtar.modell.Kolcsonzes;

import java.time.LocalDate;

/**
 * Calculates the late fee for a loan (Strategy pattern, Open/Closed):
 * a different fee policy (e.g. capped fee, grace days) is a new class,
 * the library code doesn't change.
 */
public interface KesedelmiDijSzamito {

    /** Late fee in lei if the book is returned on the given day (0 if not late). */
    long dij(Kolcsonzes kolcsonzes, LocalDate visszahozasNapja);
}