# Könyvtári rendszer fájlba mentéssel (b-11)

Konzolos könyvtári nyilvántartó program Java nyelven: könyvek, olvasók és kölcsönzések
kezelése, késedelmi díj számítása. Minden adat fájlban van, így a program túléli az
újraindítást – és ha mentés közben elmegy az áram, akkor sem vész el az adatbázis.

## Futtatás

- **NetBeans:** Run Project (F6). A bemenetet az Output ablakba kell gépelni.
- **Tesztek:** Test Project (Alt+F6), vagy parancssorból `mvn test`.
- Követelmény: JDK 17 vagy újabb, Maven.

Az adatfájl neve parancssori argumentumként adható meg (NetBeans: Properties → Run →
Arguments). **A kiterjesztés választja ki a tárolót:**

| Argumentum | Tároló |
|---|---|
| *(nincs)* | `konyvtar.csv`, saját CSV formátum |
| `valami.csv` | saját CSV formátum |
| `valami.ser` | Java szerializáció (`ObjectOutputStream`) |

Első indításkor, ha még nincs adatfájl, a program mintaadatokkal indul.
Kilépéskor automatikusan ment, ha változott valami.

### Menü

| Billentyű | Funkció |
|---|---|
| 1 / 2 | könyvek listája cím / kiadási év szerint rendezve (szabad példányokkal) |
| 3 | keresés cím- vagy szerzőrészletre (kis-nagybetű független) |
| 4 / 5 | új könyv / új olvasó felvétele |
| 6 | kölcsönzés (határidő: ma + 21 nap; ha nincs szabad példány: hibaüzenet) |
| 7 | visszahozás (késésnél kiírja a késedelmi díjat: napi 2 lej) |
| 8 / 9 | egy olvasó kölcsönzései / lejárt kölcsönzések az eddigi díjjal |
| m | mentés |
| 0 | kilépés mentéssel |

## A CSV formátum

- **Kódolás:** UTF-8, **elválasztó:** pontosvessző (`;`) – a szerzőnevekben gyakori a vessző
  (pl. „Shakespeare, William”), és a magyar Excel is ezt használja.
- **Egy sor = egy rekord.** Az első mező mondja meg, mi van a sorban:

| Sortípus | Mezők |
|---|---|
| `KONYV` | `KONYV;ISBN;cím;szerző;kiadás éve;példányszám` |
| `OLVASO` | `OLVASO;olvasójegy-szám;név` |
| `KOLCSONZES` | `KOLCSONZES;ISBN;olvasójegy-szám;kölcsönzés dátuma;határidő` |

- A dátumok formátuma `ÉÉÉÉ-HH-NN` (ISO 8601), pl. `2026-10-22`.
- A `#`-tel kezdődő sorok megjegyzések, az üres sorok kimaradnak.
- **Idézőjelezés:** ha egy mező `;`-t vagy `"`-t tartalmaz, idézőjelek közé kerül, a benne
  lévő `"` pedig megduplázódik (a szokásos CSV-szabály). Egy mezőben nem lehet sortörés.
- **A sorok sorrendje tetszőleges:** a betöltés két menetben történik, a kölcsönzéseket
  csak azután rögzíti, hogy minden könyv és olvasó ismert.
- **Betöltéskor ugyanazok a szabályok érvényesek, mint a programban:** ismétlődő ISBN,
  ismeretlen könyvre/olvasóra hivatkozó kölcsönzés vagy több kölcsönzés, mint ahány példány
  van, hibának számít.
- **Hibás fájl:** a program nem omlik össze, hanem megmondja a fájl nevét, a sor számát és a
  hiba okát, pl. `A(z) konyvtar.csv adatfájl sérült (3. sor): hibás szám: For input string: "ezer"`.
  Ilyenkor üres könyvtárral folytatja; ha ezután ment, a sérült fájlt előbb `.serult` néven
  félreteszi, így az sosem vész el.

### Példa adatfájl

A teljes fájl: [`docs/pelda-konyvtar.csv`](docs/pelda-konyvtar.csv) – kipróbálható a
`docs/pelda-konyvtar.csv` argumentummal.

```
# KonyvtariRendszer adatfájl (UTF-8, ';' elválasztó)
KONYV;978-963-11-1111-1;Egri csillagok;Gárdonyi Géza;1899;3
KONYV;978-963-11-2222-2;Az ember tragédiája;Madách Imre;1861;2
KONYV;978-963-11-3333-3;Abigél;Szabó Magda;1970;1
KONYV;978-963-11-4444-4;Ábel a rengetegben;Tamási Áron;1932;2
KONYV;978-963-11-6666-6;"Rómeó és Júlia; avagy ""a szerelem""";Shakespeare, William;1597;1
OLVASO;O-001;Kiss Anna
OLVASO;O-002;Nagy Péter
KOLCSONZES;978-963-11-1111-1;O-001;2026-10-01;2026-10-22
KOLCSONZES;978-963-11-3333-3;O-002;2026-10-20;2026-11-10
```

## Biztonságos mentés

Mindkét tároló a `BiztonsagosMentes` osztályon keresztül ír:

1. a tartalom egy **ideiglenes fájlba** kerül, ugyanabba a mappába (try-with-resources),
2. `FileChannel.force(true)` – az adat ténylegesen a lemezre kerül, nem csak a gyorsítótárba,
3. `Files.move(..., ATOMIC_MOVE)` – az ideiglenes fájl egyetlen lépésben felülírja a régit.

Az átnevezés az operációs rendszer számára oszthatatlan: aki megnyitja a fájlt, vagy a teljes
régi, vagy a teljes új változatot látja, félig írtat soha. Ha az áram az 1–2. lépés közben megy
el, csak az ideiglenes fájl sérül; hiba esetén azt a program törli is.

## A két tároló összehasonlítása

| | `SzerializaltKonyvtarTar` | `CsvKonyvtarTar` |
|---|---|---|
| Stream | `ObjectOutputStream` / `ObjectInputStream` | `BufferedWriter` / `BufferedReader` |
| Fájl | bináris, csak ez a program olvassa | szöveges, kézzel is szerkeszthető, Excelben megnyitható |
| Hibaüzenet sérült fájlnál | a hiba fajtája (csonka, nem szerializált, ismeretlen osztály) | a hibás sor száma és oka |
| Biztonság | `ObjectInputFilter`: csak a saját és a szükséges JDK-osztályok olvashatók be | – |
| Ellenőrzés | `Konyvtar.readObject` betöltés után ellenőrzi a kölcsönzéseket | betöltés a normál szabályokon (`kolcsonoz`) keresztül |

Mindkettő ugyanazt a `KonyvtarTar` interfészt valósítja meg, így a program többi része nem tudja
és nem is kell tudnia, melyik formátum van használatban.


### Object-szerződések és kollekciók

- `Konyv.equals/hashCode` csak az **ISBN**-re, `Olvaso.equals/hashCode` csak az **olvasójegy-számra**
  épül; mindkét osztály `final` és megváltoztathatatlan, így biztonságosan használható
  `HashMap`-kulcsként és `HashSet`-ben.
- Rendezés `Comparator`-ral: `Konyv.CIM_SZERINT` (magyar ábécé, `Collator`, kis-nagybetű
  független) és `Konyv.EV_SZERINT` (év, azon belül cím).
- A könyvek és olvasók `LinkedHashMap`-ben vannak (gyors keresés azonosító szerint, megőrzött
  sorrend), a kölcsönzések `ArrayList`-ben; a getterek csak olvasható nézetet adnak vissza.


## Tesztek

34 JUnit 5 teszt, a fájlkezelő tesztek `@TempDir`-rel (minden teszt saját, üres ideiglenes
mappát kap, a valódi adatfájlhoz nem nyúlnak).

| Követelmény | Teszt |
|---|---|
| mentés → betöltés után ugyanaz az állapot, **mindkét tárolóval** | `KonyvtarTarSzerzodesTest` (absztrakt szerződés-teszt), amelyet a `CsvKonyvtarTarTest` és a `SzerializaltKonyvtarTarTest` is lefuttat |
| sérült / hiányzó fájl | szemét fájl, félbevágott fájl, hibás CSV-sor (sorszámmal), több kölcsönzés, mint példány |
| biztonságos mentés | `BiztonsagosMentesTest`: írás közbeni hiba után a régi fájl ép marad, nem marad ideiglenes fájl |
| késedelmi díj | `NapiKesedelmiDijTest`, `KolcsonzesiSzolgaltatasTest` (rögzített `Clock`-kal) |
| keresés, rendezés | `KonyvtarTest` |
| kölcsönzési szabályok, equals/hashCode |
