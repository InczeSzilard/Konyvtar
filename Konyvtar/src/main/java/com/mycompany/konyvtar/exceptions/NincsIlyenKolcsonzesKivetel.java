package com.mycompany.konyvtar.exceptions;

/** Someone tries to return a book that this reader has not borrowed. */
public class NincsIlyenKolcsonzesKivetel extends KonyvtarKivetel {
    private final String isbn;
    private final String olvasojegySzam;

    public NincsIlyenKolcsonzesKivetel(String isbn, String olvasojegySzam) {
        super("A(z) " + olvasojegySzam + " olvasonal nincs kint a(z) " + isbn + " ISBN-u konyv.");
        this.isbn = isbn;
        this.olvasojegySzam = olvasojegySzam;
    }

    public String getIsbn() { return isbn; }

    public String getOlvasojegySzam() { return olvasojegySzam; }
}