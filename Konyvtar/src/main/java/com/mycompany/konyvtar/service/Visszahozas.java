package com.mycompany.konyvtar.service;

import com.mycompany.konyvtar.modell.Kolcsonzes;

import java.time.LocalDate;

/** Result of a return: the closed loan, the return day, days late and the late fee. */
public record Visszahozas(Kolcsonzes kolcsonzes, LocalDate visszahozasNapja, long kesesNapok, long dij) {

    public boolean kesett() {
        return kesesNapok > 0;
    }
}