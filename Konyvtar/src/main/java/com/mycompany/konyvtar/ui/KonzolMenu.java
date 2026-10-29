package com.mycompany.konyvtar.ui;

import com.mycompany.konyvtar.exceptions.KonyvtarKivetel;
import com.mycompany.konyvtar.exceptions.SerultAdatfajlKivetel;
import com.mycompany.konyvtar.modell.Kolcsonzes;
import com.mycompany.konyvtar.modell.Konyv;
import com.mycompany.konyvtar.modell.Konyvtar;
import com.mycompany.konyvtar.modell.Olvaso;
import com.mycompany.konyvtar.service.KolcsonzesiSzolgaltatas;
import com.mycompany.konyvtar.service.Visszahozas;
import com.mycompany.konyvtar.tar.KonyvtarTar;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/*
 * Console menu. Talks to the user, calls Konyvtar / KolcsonzesiSzolgaltatas,
 * and saves through the KonyvtarTar interface - it doesn't know which file
 * format is used. Every error is caught here and shown as a readable message.
 */
public class KonzolMenu {

    private final KonyvtarTar tar;
    private final Path adatfajl;
    private final KolcsonzesiSzolgaltatas szolgaltatas;
    private final Supplier<Konyvtar> ujKonyvtar;
    private final Bemenet bemenet;
    private final PrintStream ki;

    private Konyvtar konyvtar;
    private boolean valtozott;          // unsaved changes?
    private boolean serultFajlVolt;     // corrupt file at start: back it up before overwriting

    /**
     * @param ujKonyvtar what to start with when there is no data file yet
     *                   (main passes a library with sample data)
     */
    public KonzolMenu(KonyvtarTar tar, Path adatfajl, KolcsonzesiSzolgaltatas szolgaltatas,
                      Supplier<Konyvtar> ujKonyvtar, Bemenet bemenet, PrintStream ki) {
        this.tar = Objects.requireNonNull(tar, "tar");
        this.adatfajl = Objects.requireNonNull(adatfajl, "adatfajl");
        this.szolgaltatas = Objects.requireNonNull(szolgaltatas, "szolgaltatas");
        this.ujKonyvtar = Objects.requireNonNull(ujKonyvtar, "ujKonyvtar");
        this.bemenet = Objects.requireNonNull(bemenet, "bemenet");
        this.ki = Objects.requireNonNull(ki, "ki");
    }

    public void futtat() {
        ki.println("==================================");
        ki.println("        KONYVTARI RENDSZER");
        ki.println("==================================");
        betoltIndulaskor();
        boolean fut = true;
        while (fut) {
            try {
                fut = egyMenupont();
            } catch (Bemenet.VegeKivetel e) {
                kilepesMentessel(true);
                fut = false;
            } catch (KonyvtarKivetel e) {
                hiba(e.getMessage());
            } catch (IllegalArgumentException e) {
                hiba("Ervenytelen adat: " + e.getMessage());
            } catch (RuntimeException e) {
                hiba("Varatlan hiba tortent, a muvelet megszakadt.");
            }
        }
        ki.println("Viszontlatasra!");
    }

    // ------------------------------------------------------------ loading at start

    private void betoltIndulaskor() {
        try {
            konyvtar = tar.betolt(adatfajl);
            ki.println("Betoltve: " + adatfajl.getFileName() + " (" + konyvtar.getKonyvek().size()
                    + " konyv, " + konyvtar.getOlvasok().size() + " olvaso, "
                    + konyvtar.getKolcsonzesek().size() + " kolcsonzes)");
        } catch (NoSuchFileException e) {
            konyvtar = ujKonyvtar.get();
            valtozott = true;
            ki.println("Meg nincs adatfajl (" + adatfajl.getFileName() + "), mintaadatokkal indulunk.");
        } catch (SerultAdatfajlKivetel e) {
            konyvtar = new Konyvtar();
            serultFajlVolt = true;
            hiba(e.getMessage());
            ki.println("Ures konyvtarral folytatjuk. Ha ment, a serult fajlt elobb .serult neven felretesszuk.");
        } catch (IOException e) {
            konyvtar = new Konyvtar();
            serultFajlVolt = true;
            hiba("Az adatfajl nem olvashato: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ menu

    private boolean egyMenupont() throws KonyvtarKivetel {
        ki.println();
        ki.println("1 - Konyvek cim szerint        2 - Konyvek kiadasi ev szerint");
        ki.println("3 - Kereses (cim/szerzo)       4 - Uj konyv");
        ki.println("5 - Uj olvaso                  6 - Kolcsonzes");
        ki.println("7 - Visszahozas                8 - Olvaso kolcsonzesei");
        ki.println("9 - Lejart kolcsonzesek        m - Mentes");
        ki.println("0 - Kilepes (mentessel)");
        switch (bemenet.sor("Valasztas: ").toLowerCase()) {
            case "1" -> konyvLista(konyvtar.rendezve(Konyv.CIM_SZERINT));
            case "2" -> konyvLista(konyvtar.rendezve(Konyv.EV_SZERINT));
            case "3" -> konyvLista(konyvtar.keres(bemenet.sor("Keresett szovegreszlet: ")));
            case "4" -> ujKonyv();
            case "5" -> ujOlvaso();
            case "6" -> kolcsonzes();
            case "7" -> visszahozas();
            case "8" -> kolcsonzesLista(konyvtar.olvasoKolcsonzesei(bemenet.sor("Olvasojegy-szam: ")));
            case "9" -> kolcsonzesLista(konyvtar.lejartKolcsonzesek(szolgaltatas.ma()));
            case "m" -> mentes();
            case "0" -> {
                return !kilepesMentessel(false);
            }
            default -> ki.println("Ismeretlen menupont.");
        }
        return true;
    }

    private void konyvLista(Collection<Konyv> konyvek) {
        if (konyvek.isEmpty()) {
            ki.println("Nincs talalat.");
            return;
        }
        for (Konyv k : konyvek) {
            ki.println("  " + k.getCim() + " - " + k.getSzerzo() + " (" + k.getKiadasEve() + ")  ISBN: "
                    + k.getIsbn() + "  szabad: " + konyvtar.szabadPeldany(k) + "/" + k.getPeldanyszam());
        }
    }

    private void kolcsonzesLista(List<Kolcsonzes> lista) {
        if (lista.isEmpty()) {
            ki.println("Nincs ilyen kolcsonzes.");
            return;
        }
        for (Kolcsonzes k : lista) {
            long dij = szolgaltatas.mostaniDij(k);
            ki.println("  " + k.konyv().getCim() + " - " + k.olvaso() + ", kolcsonozve: " + k.datum()
                    + ", hatarido: " + k.hatarido() + (dij > 0 ? "  KESIK, eddigi dij: " + dij + " lej" : ""));
        }
    }

    private void ujKonyv() {
        Konyv konyv = new Konyv(
                bemenet.sor("ISBN: "),
                bemenet.sor("Cim: "),
                bemenet.sor("Szerzo: "),
                bemenet.egesz("Kiadas eve: "),
                bemenet.egesz("Peldanyszam: "));
        konyvtar.konyvHozzaad(konyv);
        valtozott = true;
        ki.println("Felveve: " + konyv);
    }

    private void ujOlvaso() {
        Olvaso olvaso = new Olvaso(bemenet.sor("Olvasojegy-szam: "), bemenet.sor("Nev: "));
        konyvtar.olvasoHozzaad(olvaso);
        valtozott = true;
        ki.println("Felveve: " + olvaso);
    }

    private void kolcsonzes() throws KonyvtarKivetel {
        Kolcsonzes k = szolgaltatas.kolcsonoz(konyvtar, bemenet.sor("ISBN: "), bemenet.sor("Olvasojegy-szam: "));
        valtozott = true;
        ki.println("Kikolcsonozve: " + k.konyv().getCim() + " - " + k.olvaso().getNev()
                + ", visszahozasi hatarido: " + k.hatarido());
    }

    private void visszahozas() throws KonyvtarKivetel {
        Visszahozas v = szolgaltatas.visszahoz(konyvtar, bemenet.sor("ISBN: "), bemenet.sor("Olvasojegy-szam: "));
        valtozott = true;
        ki.println("Visszahozva: " + v.kolcsonzes().konyv().getCim());
        if (v.kesett()) {
            ki.println("Keses: " + v.kesesNapok() + " nap, kesedelmi dij: " + v.dij() + " lej.");
        } else {
            ki.println("Hataridon belul, nincs kesedelmi dij.");
        }
    }

    // ------------------------------------------------------------ saving

    /** @return true if the save succeeded */
    private boolean mentes() {
        try {
            if (serultFajlVolt && Files.exists(adatfajl)) {
                Path felreteve = adatfajl.resolveSibling(adatfajl.getFileName() + ".serult");
                Files.move(adatfajl, felreteve, StandardCopyOption.REPLACE_EXISTING);
                ki.println("A serult fajl felreteve: " + felreteve.getFileName());
            }
            serultFajlVolt = false;
            tar.ment(konyvtar, adatfajl);
            valtozott = false;
            ki.println("Mentve: " + adatfajl.toAbsolutePath());
            return true;
        } catch (IOException | IllegalArgumentException e) {
            hiba("A mentes nem sikerult: " + e.getMessage() + " Az elozo mentes erintetlen maradt.");
            return false;
        }
    }

    /**
     * Saves before exiting - but only if something changed, so a corrupt file
     * that nobody worked on stays untouched for manual repair.
     * If saving fails, asks whether to quit anyway.
     * @param kenyszeritett true if the input ended - then we can't ask, we just try
     * @return true if the program may exit
     */
    private boolean kilepesMentessel(boolean kenyszeritett) {
        if (!valtozott) {
            return true;
        }
        if (mentes() || kenyszeritett) {
            return true;
        }
        return bemenet.igen("Kilep mentes nelkul?");
    }

    private void hiba(String uzenet) {
        ki.println("! " + uzenet);
    }
}