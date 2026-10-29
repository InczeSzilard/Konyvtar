package com.mycompany.konyvtar.tar;

import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import com.mycompany.konyvtar.modell.Konyvtar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/*
 * Storage #1: the whole Konyvtar object graph written with Java serialization
 * (ObjectOutputStream). Compact and needs no format code, but the file is
 * binary and only readable by this program.
 */
public class SzerializaltKonyvtarTar implements KonyvtarTar {

    /*
     * Only our own classes and the standard classes they consist of may be
     * deserialized. Reading arbitrary classes from a file is a known security
     * risk, so everything else is rejected.
     */
    private static final ObjectInputFilter SZURO = ObjectInputFilter.Config.createFilter(
            "maxdepth=20;com.mycompany.konyvtar.**;java.util.*;java.time.*;java.lang.*;!*");

    @Override
    public void ment(Konyvtar konyvtar, Path fajl) throws IOException {
        Objects.requireNonNull(konyvtar, "konyvtar");
        BiztonsagosMentes.ment(fajl, ki -> {
            try (ObjectOutputStream objKi = new ObjectOutputStream(new BufferedOutputStream(ki))) {
                objKi.writeObject(konyvtar);
            }
        });
    }

    @Override
    public Konyvtar betolt(Path fajl) throws IOException {
        String nev = fajl.getFileName().toString();
        try (ObjectInputStream objBe = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(fajl)))) {
            objBe.setObjectInputFilter(SZURO);
            Object beolvasott = objBe.readObject();
            if (beolvasott instanceof Konyvtar konyvtar) {
                return konyvtar;
            }
            throw new SerultAdatfajlKivetel(nev, 0, "nem konyvtari adatot tartalmaz", null);
        } catch (EOFException e) {
            throw new SerultAdatfajlKivetel(nev, 0, "a fajl csonka (varatlanul veget ert)", e);
        } catch (ObjectStreamException e) {
            // StreamCorruptedException (not a serialized file), InvalidClassException
            // (old version or rejected class), InvalidObjectException (failed integrity check)
            throw new SerultAdatfajlKivetel(nev, 0, "nem olvashato szerializalt adat (" + e.getMessage() + ")", e);
        } catch (ClassNotFoundException e) {
            throw new SerultAdatfajlKivetel(nev, 0, "ismeretlen osztaly a fajlban: " + e.getMessage(), e);
        }
    }
}