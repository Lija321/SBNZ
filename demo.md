# Demo scenariji — SBNZ pravni ekspertski sistem

Vodič za demonstraciju predmetnom asistentu. Svaki scenarij pokriva konkretna pravila iz baze znanja (vidi **Katalog pravila** u aplikaciji ili README §5).

**Preduslov:** backend i frontend pokrenuti ([RUN.md](RUN.md)) → [http://localhost:4200](http://localhost:4200)

**Opšti tok u aplikaciji:**
1. **Novi** — ručni unos predmeta  
2. **Demo** — ugrađeni primer naplate potraživanja  
3. **Sačuvaj i proceni** / **Sačuvaj izmene** — pokreće L1–L5  
4. **Ponovi procenu** — ponovna evaluacija bez izmene podataka  
5. CEP panel **+N dana** → zatim **Ponovi procenu** (vremenska pravila)

---

## Pregled — koji scenarij pokriva šta

| # | Scenarij | Pravila / nivo |
|---|----------|----------------|
| 1 | [Ugrađeni Demo](#1-ugrađeni-demo-glavni-tok) | L2, L3, L4, L5, DATES, CEP |
| 2 | [Nepotpun predmet (L1)](#2-nepotpun-predmet-l1) | L1, L5 `INCOMPLETE` |
| 3 | [Naplata potraživanja (L2 + L3)](#3-naplata-potraživanja-l2--l3) | L2, L3, L4, L5 |
| 4 | [Naknada štete (L2 + L3)](#4-naknada-štete-l2--l3) | L2, L3 |
| 5 | [Imovinski predmet (L2 + L3)](#5-imovinski-predmet-l2--l3) | L2, L3, L4 |
| 6 | [Nepoznat tip predmeta (L2)](#6-nepoznat-tip-predmeta-l2) | L2 `UNKNOWN` |
| 7 | [Klasifikacija iz početnog tipa (L2)](#7-klasifikacija-iz-početnog-tipa-l2) | L2 salience 100 |
| 8 | [Proveriti datume (L5)](#8-proveriti-datume-l5--dates) | DATES, L5 `NEEDS_DATE_CHECK` |
| 9 | [Spreman za pregled (L5)](#9-spreman-za-pregled-l5) | L1–L5 kompletno |
| 10 | [Prioritet statusa (L5)](#10-prioritet-statusa-l5) | L5 kandidati |
| 11 | [Opterećenje kancelarije (L4)](#11-opterećenje-kancelarije-l4) | L4 accumulate |
| 12 | [CEP — dokument nije dodat](#12-cep--dokument-nije-dodat) | CEP §1 |
| 13 | [CEP — predmet čeka predugo](#13-cep--predmet-čeka-predugo) | CEP §2 |
| 14 | [CEP — spreman, nije pregledan](#14-cep--spreman-nije-pregledan) | CEP §3 |
| 15 | [CEP — stara neaktivnost](#15-cep--stara-neaktivnost) | CEP §4, `CaseInactive` |
| 16 | [Arhiviran predmet](#16-arhiviran-predmet) | L5 `ARCHIVED` |
| 17 | [Transparentnost](#17-transparentnost-audit--pravila) | Audit, rule firings |

**Preporučen redosled za odbranu (15–20 min):** 1 → 12 → 10 → 17, zatim po izboru 2, 4, 8 ili 9.

---

## 1. Ugrađeni Demo (glavni tok)

**Cilj:** Brza demonstracija celog sistema jednim klikom.

### Koraci
1. Klik **Demo**
2. Pregled desnog panela — **Pregled**, **Za pažnju**, **Predložene akcije**
3. Otvori **Detalji rezonovanja (L1–L5)** — kandidati, čeklista
4. CEP panel → **+7 dana** → **Ponovi procenu**

### Očekivano (pre +7 dana)

| Oblast | Rezultat |
|--------|----------|
| L2 | `DEBT_COLLECTION`, pouzdanost HIGH |
| L3 | Nedostaje `SERVICE_PROOF` (obavezno); očekivana opomena, podaci o firmi |
| L4 | Dokumentacija **delimična** (1 nedostajući obavezan) |
| L5 | Status **Čeka dopunu od stranke** (prioritet 2) |
| DATES | Datum dospelosti → zadatak *Proveriti datum dospelosti* |
| Zadaci | `REQUEST_SERVICE_PROOF`, `CHECK_IF_NOTICE_EXISTS`, `CHECK_COMPANY_DATA`, `VERIFY_DUE_DATE` |

### Očekivano (posle +7 dana + Ponovi procenu)

| Oblast | Rezultat |
|--------|----------|
| CEP | Alarm **Dokument nije dodat u roku** |
| L5 | Status **Zahteva pažnju** (CEP ima prioritet 3 > čekanje dokumentacije) |
| Zadaci | + `CHECK_DOCUMENT_REQUEST_STATUS` |

### Šta reći asistentu
> Sistem ne odlučuje umesto pravnika — klasifikuje predmet, proverava čeklistu, predlaže administrativne zadatke i prati rokove kroz CEP.

---

## 2. Nepotpun predmet (L1)

**Cilj:** Validacija osnovnih podataka — bez kompletnih podataka predmet ne može dalje.

### Unos (Novi predmet)

| Polje | Vrednost |
|-------|----------|
| Naziv | *(prazno)* |
| Opis | *(prazno)* |
| Stranka | Ime bez kontakta |
| Druga strana | *(prazno)* |
| Indikatori | svi isključeni |

Klik **Sačuvaj i proceni**.

### Očekivano

- **Za pažnju / akcije:** nedostaju naziv, opis, druga strana, kontakt (L1)
- **Status (L5):** **Nepotpun** (prioritet 1 — najviši)
- U **Detaljima:** kandidat `INCOMPLETE`

### Pokrivena pravila
`Missing case name`, `Missing description`, `Missing opposing party`, `Missing contact`, `Candidate incomplete`

---

## 3. Naplata potraživanja (L2 + L3)

**Cilj:** Ručno kreiranje predmeta istog tipa kao Demo, sa varijantama dokumentacije.

### Varijanta A — ista situacija kao Demo (ručno)

| Polje | Vrednost |
|-------|----------|
| Naziv | Naplata po fakturi |
| Opis | Sporno potraživanje |
| Tip stranke | Pravno lice |
| Stranka + kontakt | popunjeno |
| Druga strana | Dužnik d.o.o. |
| Indikatori | Potraživanje ✓, Faktura ✓, Ugovor ✓ |
| Datumi | Datum dospelosti: bilo koji |
| Dokumenti (naplata) | Faktura ✓, Ugovor ✓, Dokaz o usluzi ✗ |
| Dokumenti | Opomena ✗ |

**Očekivano:** L2 `DEBT_COLLECTION`, L3 nedostaje `SERVICE_PROOF`, status **Čeka dopunu**.

### Varijanta B — više nedostajućih (L4 INCOMPLETE)

Isto kao A, ali **isključi** i Fakturu i Ugovor (sva tri obavezna nedostaju).

**Očekivano:** L4 dokumentacija **nepotpuna** (>2 nedostajuća), više zadataka `REQUEST_*`.

### Pokrivena pravila
L2 `Classify debt collection`, L3 template pravila za `DEBT_COLLECTION`, L4 `Documentation partial/incomplete`

---

## 4. Naknada štete (L2 + L3)

**Cilj:** Drugi tip predmeta — šteta.

### Unos

| Polje | Vrednost |
|-------|----------|
| Naziv | Naknada štete — saobraćaj |
| Opis | Potraživanje štete od osiguravača |
| Tip stranke | Fizičko lice |
| Stranka, kontakt, druga strana | popunjeno |
| Indikatori | Šteta ✓ |
| Datumi | Datum nastanka štete |
| Dokumenti (šteta) | Zapisnik o šteti ✓, Dokaz o šteti ✗ |

Klik **Sačuvaj i proceni**.

### Očekivano

| Oblast | Rezultat |
|--------|----------|
| L2 | `DAMAGES`, HIGH (zapisnik zadovoljava uslov klasifikacije) |
| L3 | Nedostaje `DAMAGE_PROOF` → `REQUEST_DAMAGE_PROOF` |
| DATES | *Proveriti datum štete* |
| L5 | **Čeka dopunu** (nedostaje obavezni dokaz) |

### Pokrivena pravila
`Classify damages`, `damages missing damage_proof`, `Damage date needs check`

---

## 5. Imovinski predmet (L2 + L3)

**Cilj:** Nepokretnost / katastar.

### Unos

| Polje | Vrednost |
|-------|----------|
| Naziv | Spor oko vlasništva |
| Opis | Prekid uspostave granice |
| Stranke | popunjeno |
| Indikatori | Nepokretnost ✓ ili Katastarski podaci ✓ |
| Dokumenti (nepokretnost) | sve ✗ |

### Očekivano

- L2: `PROPERTY`, HIGH  
- L3: nedostaju `CADASTRE_EXTRACT` i `OWNERSHIP_DOCUMENT`  
- L4: dokumentacija **delimična** ili **nepotpuna** (2 obavezna)  
- Zadaci: `REQUEST_CADASTRE_EXTRACT`, `REQUEST_OWNERSHIP_DOCUMENT`

### Napomena — javni organ
Ako je **tip stranke = Javni organ**, dodatno se očekuje provera `AUTHORIZATION_OR_DECISION` → zadatak `CHECK_AUTHORIZATION_DOCUMENT` (očekivani dokument).

---

## 6. Nepoznat tip predmeta (L2)

**Cilj:** Kada indikatori ne pokrivaju nijednu klasu.

### Unos

| Polje | Vrednost |
|-------|----------|
| Naziv, opis, stranke | minimalno popunjeno (L1 OK) |
| Početni tip | Nepoznat |
| Indikatori | **svi isključeni** |
| Dokumenti | prazno |

### Očekivano

- L2: `UNKNOWN`, LOW  
- Zadatak: **Ručna provera tipa predmeta** (`MANUAL_CASE_TYPE_REVIEW`)  
- Status zavisi od dokumentacije; bez dokumenata često **Čeka dopunu** ili samo zadaci

### Pokrivena pravila
`Unknown case type`

---

## 7. Klasifikacija iz početnog tipa (L2)

**Cilj:** Pravilo sa salience 100 — ručno zadat tip ima prednost nad indikatorima.

### Unos

| Polje | Vrednost |
|-------|----------|
| Početni tip predmeta | **Naknada štete** |
| Indikatori | Potraživanje ✓ (kontradiktorno) |
| Ostalo | L1 popunjeno |

### Očekivano

- L2: `DAMAGES` (iz početnog tipa, ne `DEBT_COLLECTION` iz indikatora)

### Pokrivena pravila
`Classify from initial case type`

---

## 8. Proveriti datume (L5 + DATES)

**Cilj:** Status `NEEDS_DATE_CHECK` kada nema jačih blokera.

### Unos

Kompletan predmet naplate **bez nedostajućih obaveznih dokumenata**:

| Polje | Vrednost |
|-------|----------|
| Indikatori | Potraživanje ✓, Faktura ✓ |
| Dokumenti (naplata) | Faktura ✓, Ugovor ✓, Dokaz o usluzi ✓ |
| Dokumenti | Opomena ✓, Podaci o firmi ✓ (pravno lice) |
| Datumi | **Datum dospelosti** popunjen; ostali prazni |

### Očekivano

- DATES: *Proveriti datum dospelosti*  
- L5 kandidat: **Proveriti datume** (prioritet 4)  
- **Status:** **Proveriti datume** — jer nema L1/L3 blokera  

### Varijanta — više datuma
Dodaj i **datum prijema odluke** i **datum štete** → tri VERIFY zadatka u **Predloženim akcijama**.

### Pokrivena pravila
`Due / Decision / Damage date needs check`, `Candidate needs date check`

---

## 9. Spreman za pregled (L5)

**Cilj:** Jedini „zeleni“ status — sve provere prošle.

### Unos

| Polje | Vrednost |
|-------|----------|
| Naziv, opis, stranke, kontakt | popunjeno |
| Tip stranke | Pravno lice |
| Indikatori | Potraživanje ✓, Faktura ✓, Ugovor ✓ |
| Dokumenti (naplata) | Faktura ✓, Ugovor ✓, Dokaz o usluzi ✓, Opomena ✓ |
| Identitet / firma | Podaci o registraciji firme ✓ |
| Datumi | **prazno** (datumi otvaraju VERIFY zadatke koji blokiraju spremnost) |

### Očekivano

- L1: osnovni podaci kompletni  
- L4: dokumentacija **kompletna**  
- L5: status **Spreman za pregled**  
- Nema otvorenih zadataka u pregledu  

### Šta reći asistentu
> Predmet može na inicijalni pregled pravnika; sistem i dalje ne zamenjuje stručnu procenu.

---

## 10. Prioritet statusa (L5)

**Cilj:** Objasniti zašto jedan predmet ima više kandidata, a jedan status pobedi.

### Priprema
Koristi **Demo** predmet (čeka dokument + datum za proveru).

### Koraci
1. Otvori **Detalji rezonovanja → Kandidati za status (L5)**  
2. Uoči više kandidata (npr. čeka dopunu p2, proveriti datume p4)  
3. Objasni: niži broj prioriteta = jači signal → **Čeka dopunu** pobedi  
4. **+7 dana** → **Ponovi procenu**  
5. Kandidat **Zahteva pažnju** (p3) preuzima status zbog CEP alarma  

### Tabela prioriteta (za referencu)

| Prioritet | Status |
|-----------|--------|
| 1 | Nepotpun |
| 2 | Čeka dopunu od stranke |
| 3 | Zahteva pažnju |
| 4 | Proveriti datume |
| 5 | Spreman za pregled |
| 6 | Arhiviran |

---

## 11. Opterećenje kancelarije (L4)

**Cilj:** Zbirna pravila — upozorenje na nivo kancelarije.

**Prag:** `office.load.waitingThreshold=2` (min. 2 predmeta u statusu čekanja).

### Koraci
1. Klik **Demo** (predmet 1 — čeka dopunu)  
2. **Novi** → napravi predmet 2 isto kao varijanta A u [scenariju 3](#3-naplata-potraživanja-l2--l3) (nedostaje dokaz o usluzi)  
3. **Ponovi procenu** na bilo kom od njih  

### Očekivano

- U rezultatima: **Opterećenje kancelarije (L4)** — mnogo predmeta čeka dokumentaciju  

### Varijanta — spremni predmeti
Napravi **2× scenarij 9** (spreman za pregled). Prag `office.load.readyThreshold=2` → upozorenje *mnogo spremnih predmeta*.

### Pokrivena pravila
`Many cases waiting for client`, `Many ready cases`

---

## 12. CEP — dokument nije dodat

**Cilj:** Vremensko praćenje zahteva za dokument.

### Koraci
1. **Demo**  
2. Proveri da postoji zadatak *Zatražiti dokaz o usluzi* i nedostaje `SERVICE_PROOF`  
3. CEP → **+7 dana**  
4. **Ponovi procenu**

### Očekivano

- CEP alarm: **Dokument nije dodat u roku**  
- Zadatak: `CHECK_DOCUMENT_REQUEST_STATUS`  
- Status: **Zahteva pažnju**

### Uslov pravila
`TaskCreatedEvent` za dokument + ≥7 dana + dokument i dalje nedostaje.

---

## 13. CEP — predmet čeka predugo

**Cilj:** Dugotrajno čekanje dopune od stranke.

### Koraci
1. **Demo** (status čeka dopunu)  
2. CEP → **+14 dana**  
3. **Ponovi procenu** (bez izmene predmeta)

### Očekivano

- CEP alarm: **Predmet predugo čeka klijenta**  
- Zadatak: `CHECK_CASE_STATUS`  
- Status: **Zahteva pažnju**

### Napomena
Pravilo gleda da nema `CaseUpdatedEvent` / `DocumentAddedEvent` u poslednjih 14 dana (simulacionog vremena).

---

## 14. CEP — spreman, nije pregledan

**Cilj:** Predmet spreman ali zastareo bez pregleda pravnika.

### Priprema
Kreiraj predmet po [scenariju 9](#9-spreman-za-pregled-l5) — status **Spreman za pregled**.

### Koraci
1. CEP → **+7 dana**  
2. **Ponovi procenu** (ne menjaj predmet)

### Očekivano

- CEP alarm: **Spreman predmet nije pregledan**  
- Zadatak: **Dodeliti pregled pravniku** (`ASSIGN_REVIEW`)

---

## 15. CEP — stara neaktivnost

**Cilj:** Prag neaktivnosti (`case.inactivity.days=30`).

### Koraci
1. Bilo koji sačuvan predmet (npr. Demo)  
2. CEP → **+30 dana**  
3. **Ponovi procenu**

### Očekivano

- Blok **Neaktivnost predmeta** (npr. 30 dana)  
- Zadatak: `CHECK_CASE_STATUS`  
- L5 kandidat: **Zahteva pažnju** (preko `CaseInactive`)

### Pokrivena pravila
`Old case activity`, `Candidate needs attention from inactivity`

---

## 16. Arhiviran predmet

**Cilj:** Status arhive isključuje predmet iz aktivne obrade.

### Koraci
1. Učitaj bilo koji predmet (iz liste levo)  
2. U formi: **Status predmeta → Arhiviran** ✓  
3. **Sačuvaj izmene**

### Očekivano

- Status: **Arhiviran** (prioritet 6 u kandidatima)  
- Predmet se ne tretira kao spreman za dalju automatsku procenu

### Pokrivena pravila
`Candidate archived`

---

## 17. Transparentnost (audit + pravila)

**Cilj:** Objašnjivost odluka — zaštita od „crne kutije“.

### Koraci
1. Posle bilo koje procene otvori **Transparentnost (pravila i audit)**  
2. Pregled **Aktiviranih pravila** — imena DRL pravila  
3. Pregled **Audit loga** — promene statusa, zadaci, pravila  
4. Uporedi sa **Katalogom pravila** u zaglavlju  

### Šta reći asistentu
> Svaka odluka je traga u audit logu; asistent vidi koja su se pravila aktivirala i zašto je predložen zadatak.

---

## Brza mapa pravila → scenarij

| Grupa pravila | Scenarij |
|---------------|----------|
| L1 validacija | 2 |
| L2 klasifikacija (debt / damages / property / unknown / initial) | 1, 3, 4, 5, 6, 7 |
| L3 checklist (11 template pravila) | 1, 3, 4, 5 |
| L4 accumulate (dokumentacija + opterećenje) | 1, 3, 5, 11 |
| L5 status | 1–11, 16 |
| DATES | 1, 4, 8 |
| CEP (4 pravila) | 1, 12, 13, 14, 15 |
| Audit / transparentnost | 17 |

---

## Rešavanje problema pri demo-u

| Problem | Rešenje |
|---------|---------|
| CEP alarm se ne pojavi posle +7 | Klikni **Ponovi procenu** posle pomeranja sata |
| Status ostaje „Čeka dopunu“ posle CEP | Očekivano dok CEP alarm nije jači (p3); posle +7 treba „Zahteva pažnju“ |
| Ne mogu „Spreman za pregled“ | Proveri sve obavezne **i** očekivane dokumente; **ne unosi datume** (otvaraju VERIFY zadatke) |
| L4 opterećenje se ne vidi | Potrebna su **2 predmeta** u istom statusu (čekanje ili spreman) |
| Backend greška | Restart: `docker compose up -d` + `./mvnw spring-boot:run` ([RUN.md](RUN.md)) |

---

## Napomena o backward chainingu

BC upiti (`isCaseReadyForInitialReview`, rekurzivni `isCaseProcessable`) trenutno **nisu u frontendu** — Drools ne izvršava rekurzivne DRL upite. Za demonstraciju koristi L5 status i blok **Predložene akcije** kao ekvivalent „da li je predmet spreman“.
