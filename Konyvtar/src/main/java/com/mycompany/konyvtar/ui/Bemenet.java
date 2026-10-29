package com.mycompany.konyvtar.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Reads the user's input safely. The stream is passed in (not System.in
 * directly), so the menu could also be driven by prepared input.
 */
public class Bemenet {

    /** No more input (end of stream): the program should exit. */
    public static class VegeKivetel extends RuntimeException {
        public VegeKivetel() {
            super("Nincs tobb bemenet.");
        }
    }

    private final BufferedReader olvaso;
    private final PrintStream ki;

    public Bemenet(InputStream be, PrintStream ki) {
        this.olvaso = new BufferedReader(new InputStreamReader(be));
        this.ki = ki;
    }

    public String sor(String kerdes) {
        ki.print(kerdes);
        ki.flush();
        try {
            String sor = olvaso.readLine();
            if (sor == null) {
                throw new VegeKivetel();
            }
            return sor.trim();
        } catch (IOException e) {
            throw new VegeKivetel();
        }
    }

    public int egesz(String kerdes) {
        String sor = sor(kerdes);
        try {
            return Integer.parseInt(sor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + sor + "\" nem ervenyes egesz szam.");
        }
    }

    public boolean igen(String kerdes) {
        return sor(kerdes + " (i/n): ").equalsIgnoreCase("i");
    }
}