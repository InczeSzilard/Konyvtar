

package com.mycompany;

import com.mycompany.konyvtar.modell.Konyv;
import com.mycompany.konyvtar.modell.Konyvtar;
import com.mycompany.konyvtar.modell.Olvaso;
import com.mycompany.konyvtar.rule.NapiKesedelmiDij;
import com.mycompany.konyvtar.service.KolcsonzesiSzolgaltatas;
import com.mycompany.konyvtar.tar.CsvKonyvtarTar;
import com.mycompany.konyvtar.tar.KonyvtarTar;
import com.mycompany.konyvtar.tar.SzerializaltKonyvtarTar;
import com.mycompany.konyvtar.ui.Bemenet;
import com.mycompany.konyvtar.ui.KonzolMenu;

import java.nio.file.Path;
import java.time.Clock;

/*
 * Entry point and composition root: the only place where concrete classes
 * are chosen and wired together.
 *
 * The data file can be given as an argument; its extension picks the storage:
 *   .ser -> Java serialization,  anything else -> CSV.
 * Default: konyvtar.csv in the working folder.
 *
 * @author szila
 */
public class KonyvtariRendszer {

    private static final int KOLCSONZESI_NAPOK = 21;
    private static final long NAPI_KESEDELMI_DIJ = 2; // lei / day

    public static void main(String[] args) {
        try {
            Path adatfajl = Path.of(args.length > 0 ? args[0] : "konyvtar.csv");
            KonyvtarTar tar = adatfajl.toString().toLowerCase().endsWith(".ser")
                    ? new SzerializaltKonyvtarTar()
                    : new CsvKonyvtarTar();

            KolcsonzesiSzolgaltatas szolgaltatas = new KolcsonzesiSzolgaltatas(
                    Clock.systemDefaultZone(), KOLCSONZESI_NAPOK, new NapiKesedelmiDij(NAPI_KESEDELMI_DIJ));

            new KonzolMenu(tar, adatfajl, szolgaltatas, KonyvtariRendszer::mintaKonyvtar,
                    new Bemenet(System.in, System.out), System.out).futtat();
        } catch (RuntimeException e) {
            // last safety net: no stack trace even if startup fails
            System.out.println("A program nem tudott elindulni: " + e.getMessage());
        }
    }

    /** Sample data for the very first start, when there is no data file yet. */
    private static Konyvtar mintaKonyvtar() {
        Konyvtar k = new Konyvtar();
        k.konyvHozzaad(new Konyv("978-963-11-1111-1", "Egri csillagok", "Gardonyi Geza", 1899, 3));
        k.konyvHozzaad(new Konyv("978-963-11-2222-2", "Az ember tragediaja", "Madach Imre", 1861, 2));
        k.konyvHozzaad(new Konyv("978-963-11-3333-3", "Abigel", "Szabo Magda", 1970, 1));
        k.konyvHozzaad(new Konyv("978-963-11-4444-4", "Abel a rengetegben", "Tamasi Aron", 1932, 2));
        k.konyvHozzaad(new Konyv("978-963-11-5555-5", "A Pal utcai fiuk", "Molnar Ferenc", 1906, 2));
        k.olvasoHozzaad(new Olvaso("O-001", "Kiss Anna"));
        k.olvasoHozzaad(new Olvaso("O-002", "Nagy Peter"));
        return k;
    }
}