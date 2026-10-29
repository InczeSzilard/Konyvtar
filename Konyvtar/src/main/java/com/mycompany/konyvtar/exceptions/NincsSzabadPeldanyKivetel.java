package com.mycompany.konyvtar.exceptions;

/** Every copy of the book is currently borrowed. */
public class NincsSzabadPeldanyKivetel extends KonyvtarKivetel {
    private final String isbn;

    public NincsSzabadPeldanyKivetel(String isbn, String cim) {
        super("A(z) \"" + cim + "\" (" + isbn + ") konyvbol jelenleg nincs szabad peldany.");
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }
}