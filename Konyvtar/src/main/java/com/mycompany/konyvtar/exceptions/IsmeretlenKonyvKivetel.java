package com.mycompany.konyvtar.exceptions;

/** No book with this ISBN exists in the library. */
public class IsmeretlenKonyvKivetel extends KonyvtarKivetel {
    private final String isbn;

    public IsmeretlenKonyvKivetel(String isbn) {
        super("Nincs ilyen ISBN-u konyv: " + isbn);
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }
}