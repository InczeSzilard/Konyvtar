package com.mycompany.konyvtar.rule;

import com.mycompany.konyvtar.modell.Kolcsonzes;

import java.time.LocalDate;

/** Fixed fee for every day after the due date. */
public class NapiKesedelmiDij implements KesedelmiDijSzamito {

    private final long napiDij;

    public NapiKesedelmiDij(long napiDij) {
        if (napiDij < 0) {
            throw new IllegalArgumentException("A napi dij nem lehet negativ: " + napiDij);
        }
        this.napiDij = napiDij;
    }

    @Override
    public long dij(Kolcsonzes kolcsonzes, LocalDate visszahozasNapja) {
        return kolcsonzes.kesesNapokban(visszahozasNapja) * napiDij;
    }

    public long getNapiDij() {
        return napiDij;
    }
}