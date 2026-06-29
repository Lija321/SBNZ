# Demo uputstvo — SBNZ pravni ekspertski sistem

Vodič za demonstraciju predmetnom asistentu. Svaki scenarij pokriva konkretna pravila iz baze znanja (vidi **Katalog pravila** u zaglavlju aplikacije ili README §5).

---

## Pokretanje (preduslov)

```bash
# 1. Baza
docker compose up -d

# 2. Backend (port 8080)
cd back-end && ./mvnw spring-boot:run

# 3. Frontend (port 4200, proxy ka backendu)
cd front-end && npm install && npm start
```

Otvori [http://localhost:4200](http://localhost:4200).

---

## Kontrole u aplikaciji

| Akcija | Gde | Šta radi |
|--------|-----|----------|
| **Novi** | Zaglavlje | Prazna forma za ručni unos |
| **Demo** | Zaglavlje | Ugrađeni predmet naplate potraživanja |
| **Sačuvaj i proceni** | Forma | Kreira predmet → L1–L5 evaluacija |
| **Sačuvaj izmene** | Forma (edit) | Ažurira predmet + ponovna evaluacija |
| **Ponovi procenu** | Zaglavlje | Evaluacija bez izmene podataka |
| **+N dana / Reset** | CEP panel | Pomeranje simulacionog sata |
| **Katalog pravila** | Zaglavlje | Pregled svih pravila po nivoima |
| **BC panel** | Ispod forme | Backward-chaining upiti (kad je predmet izabran) |

**Važno za CEP:** posle pomeranja sata uvek klikni **Ponovi procenu** (ili izaberi predmet ponovo) da se vremenska pravila ponovo izvrše.

---

## Pregled scenarija

| # | Scenarij | Pravila / nivo |
|---|----------|----------------|
| 1 | [Ugrađeni Demo](#1-ugrađeni-demo-glavni-tok) | L2, L3, L4, L5, DATES, CEP |
| 2 | [Nepotpun predmet (L1)](#2-nepotpun-predmet-l1) | L1, L5 `INCOMPLETE` |
| 3 | [Naplata potraživanja (L2 + L3)](#3-naplata-potraživanja-l2--l3) | L2, L3, L4, L5 |
| 4 | [Naknada štete (L2 + L3)](#4-naknada-štete-l2--l3) | L2, L3, DATES |
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
| 18 | [Backward chaining — spreman?](#18-backward-chaining--spreman-za-pregled) | BC upit `isCaseReadyForInitialReview` |
| 19 | [Backward chaining — glavna radnja](#19-backward-chaining--glavna-pravna-radnja) | BC + `procedure_rules.drl` |

**Preporučen redosled za odbranu (15–20 min):** 1 → 12 → 10 → 18 → 19 → 17. Po izboru dodaj 2, 4, 8 ili 9.

---

## Brzi script za odbranu (≈15 min)

1. **Demo** → pokaži status, zadatke, klasifikaciju (2 min)
2. CEP **+7 dana** → **Ponovi procenu** → CEP alarm (2 min)
3. **Detalji rezonovanja** → kandidati za status, prioritet (2 min)
4. BC panel → **Spreman za inicijalni pregled?** na Demo predmetu → `false` + podciljevi (2 min)
5. BC panel → **Da li je moguća glavna pravna radnja?** → blokirana zbog opomene (3 min)
6. **Transparentnost** → aktivirana pravila + audit (2 min)
7. *(opciono)* **Novi** → prazan predmet → L1 **Nepotpun** (2 min)

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
| Procedure | Glavna radnja **Podnošenje tužbe** — **blokirana** |
| Zadaci | `REQUEST_SERVICE_PROOF`, `CHECK_IF_NOTICE_EXISTS`, `CHECK_COMPANY_DATA`, `VERIFY_DUE_DATE` |

### Očekivano (posle +7 dana + Ponovi procenu)

| Oblast | Rezultat |
|--------|----------|
| CEP | Alarm **Dokument nije dodat u roku** |
| L5 | Status **Zahteva pažnju** (CEP prioritet 3 > čekanje dokumentacije) |
| Zadaci | + `CHECK_DOCUMENT_REQUEST_STATUS` |

### Šta reći asistentu
> Sistem ne donosi pravno mišljenje — klasifikuje predmet, proverava čeklistu, predlaže administrativne zadatke i prati rokove kroz CEP.

---

## 2. Nepotpun predmet (L1)

**Cilj:** Validacija osnovnih podataka.

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
- **Predložene akcije:** nedostaju naziv, opis, druga strana, kontakt
- **Status:** **Nepotpun** (prioritet 1)
- **Detalji:** kandidat `INCOMPLETE`

### Pokrivena pravila
`Missing case name`, `Missing description`, `Missing opposing party`, `Missing contact`, `Candidate incomplete`

---

## 3. Naplata potraživanja (L2 + L3)

### Varijanta A — kao Demo (ručno)

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

**Očekivano:** L2 `DEBT_COLLECTION`, nedostaje `SERVICE_PROOF`, status **Čeka dopunu**.

### Varijanta B — više nedostajućih (L4 INCOMPLETE)

Isto kao A, ali isključi i Fakturu i Ugovor.

**Očekivano:** L4 **nepotpuna** dokumentacija (>2 nedostajuća), više `REQUEST_*` zadataka.

### Pokrivena pravila
`Classify debt collection`, L3 template za `DEBT_COLLECTION`, L4 `Documentation partial/incomplete`

---

## 4. Naknada štete (L2 + L3)

| Polje | Vrednost |
|-------|----------|
| Naziv | Naknada štete — saobraćaj |
| Opis | Potraživanje štete od osiguravača |
| Tip stranke | Fizičko lice |
| Stranka, kontakt, druga strana | popunjeno |
| Indikatori | Šteta ✓ |
| Datumi | Datum nastanka štete |
| Dokumenti (šteta) | Zapisnik ✓, Dokaz o šteti ✗ |

### Očekivano
- L2: `DAMAGES`, HIGH
- L3: `REQUEST_DAMAGE_PROOF`, `CHECK_IF_DAMAGE_REPORT_EXISTS` (očekivani zapisnik već ✓ — provera se ne aktivira)
- DATES: *Proveriti datum štete*
- L5: **Čeka dopunu**

---

## 5. Imovinski predmet (L2 + L3)

| Polje | Vrednost |
|-------|----------|
| Naziv | Spor oko vlasništva |
| Opis | Prekid uspostave granice |
| Stranke | popunjeno |
| Indikatori | Nepokretnost ✓ ili Katastar ✓ |
| Dokumenti (nepokretnost) | sve ✗ |

### Očekivano
- L2: `PROPERTY`, HIGH
- L3: `REQUEST_CADASTRE_EXTRACT`, `REQUEST_OWNERSHIP_DOCUMENT`
- L4: **delimična** ili **nepotpuna** (2 obavezna)

### Javni organ (dodatno L3 pravilo)
| Polje | Vrednost |
|-------|----------|
| Tip stranke (klijent) | **Javni organ** |
| Dokumenti | bez `AUTHORIZATION_OR_DECISION` |

**Očekivano:** zadatak `CHECK_AUTHORIZATION_DOCUMENT`

---

## 6. Nepoznat tip predmeta (L2)

| Polje | Vrednost |
|-------|----------|
| Naziv, opis, stranke | minimalno (L1 OK) |
| Početni tip | Automatska klasifikacija |
| Indikatori | **svi isključeni** |

### Očekivano
- L2: `UNKNOWN`, LOW
- Zadatak: **Ručna provera tipa predmeta**

---

## 7. Klasifikacija iz početnog tipa (L2)

| Polje | Vrednost |
|-------|----------|
| Početni tip predmeta | **Naknada štete** |
| Indikatori | Potraživanje ✓ (kontradiktorno) |

### Očekivano
- L2: `DAMAGES` (iz dropdown-a, ne iz indikatora)

Pravilo: `Classify from initial case type` (salience 100)

---

## 8. Proveriti datume (L5 + DATES)

Kompletan predmet naplate **sa svim obaveznim dokumentima**:

| Polje | Vrednost |
|-------|----------|
| Indikatori | Potraživanje ✓, Faktura ✓ |
| Dokumenti (naplata) | Faktura ✓, Ugovor ✓, Dokaz o usluzi ✓, Opomena ✓ |
| Identitet | Podaci o registraciji firme ✓ |
| Datumi | **Datum dospelosti**; ostali prazni |

### Očekivano
- DATES: *Proveriti datum dospelosti*
- L5: **Proveriti datume** (prioritet 4, jer nema L1/L3 blokera)

### Varijanta — tri datuma
Dodaj datum dospelosti, datum štete i datum prijema odluke → tri VERIFY zadatka.

---

## 9. Spreman za pregled (L5)

**Cilj:** Status **Spreman za pregled** — zahteva pažljivo popunjavanje.

| Polje | Vrednost |
|-------|----------|
| Naziv, opis, stranke, kontakt | popunjeno |
| Tip stranke | Pravno lice |
| Indikatori | Potraživanje ✓, Faktura ✓, Ugovor ✓ |
| Dokumenti (naplata) | sva četiri ✓ (uključujući opomena) |
| Identitet | Podaci o registraciji firme ✓ |
| Datumi | **prazno** (datumi otvaraju VERIFY zadatke koji blokiraju spremnost) |

### Očekivano
- L4: dokumentacija **kompletna**
- L5: **Spreman za pregled**
- Nema otvorenih zadataka

> Datumi i očekivani dokumenti generišu otvorene zadatke — zato ih za ovaj scenarij ostavi prazne / označene.

---

## 10. Prioritet statusa (L5)

### Priprema
Koristi **Demo** predmet.

### Koraci
1. **Detalji rezonovanja → Kandidati za status**
2. Uoči više kandidata (npr. čeka dopunu p2, proveriti datume p4)
3. Objasni: **niži broj prioriteta = jači signal**
4. **+7 dana** → **Ponovi procenu**
5. Kandidat **Zahteva pažnju** (p3) preuzima zbog CEP alarma

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

**Prag:** min. **2 predmeta** u istom statusu (`office.load.*Threshold=2`).

### Koraci
1. **Demo** (predmet 1 — čeka dopunu)
2. **Novi** → predmet 2 kao [scenarij 3A](#3-naplata-potraživanja-l2--l3)
3. **Ponovi procenu** na bilo kom

### Očekivano
- **Opterećenje kancelarije (L4)** — mnogo predmeta čeka dokumentaciju

### Varijanta
Napravi **2× scenarij 9** → upozorenje *mnogo spremnih predmeta*.

---

## 12. CEP — dokument nije dodat

1. **Demo**
2. Proveri zadatak *Zatražiti dokaz o usluzi*
3. CEP → **+7 dana**
4. **Ponovi procenu**

### Očekivano
- Alarm: **Dokument nije dodat u roku**
- Status: **Zahteva pažnju**

Uslov: `TaskCreatedEvent` + ≥7 dana + dokument i dalje nedostaje.

---

## 13. CEP — predmet čeka predugo

1. **Demo** (status čeka dopunu)
2. CEP → **+14 dana**
3. **Ponovi procenu** (bez izmene predmeta)

### Očekivano
- Alarm: **Predmet predugo čeka klijenta**
- Zadatak: `CHECK_CASE_STATUS`

---

## 14. CEP — spreman, nije pregledan

### Priprema
Kreiraj predmet po [scenariju 9](#9-spreman-za-pregled-l5).

### Koraci
1. CEP → **+7 dana**
2. **Ponovi procenu**

### Očekivano
- Alarm: **Spreman predmet nije pregledan**
- Zadatak: **Dodeliti pregled pravniku**

---

## 15. CEP — stara neaktivnost

1. Bilo koji sačuvan predmet (npr. Demo)
2. CEP → **+30 dana**
3. **Ponovi procenu**

### Očekivano
- **Neaktivnost predmeta** (npr. 30 dana)
- L5 kandidat: **Zahteva pažnju**

Pravilo: `Old case activity` (gleda `lastUpdatedAt` i 30d prozor bez događaja).

---

## 16. Arhiviran predmet

1. Učitaj predmet iz liste levo
2. Forma → **Arhiviran** ✓
3. **Sačuvaj izmene**

### Očekivano
- Status: **Arhiviran** (prioritet 6)

---

## 17. Transparentnost (audit + pravila)

1. Posle bilo koje procene → **Transparentnost (pravila i audit)**
2. **Aktivirana pravila** — imena DRL pravila (L3 template imena su npr. `DEBT_COLLECTION_missing_CONTRACT_2`)
3. **Audit log** — promene statusa, zadaci
4. Uporedi sa **Katalogom pravila**

---

## 18. Backward chaining — spreman za pregled?

**Cilj:** BC upit `isCaseReadyForInitialReview`.

### Koraci
1. **Demo** predmet (nije spreman)
2. Skroluj do **Backward chaining** panela
3. Klik **Spreman za inicijalni pregled?**

### Očekivano (Demo)
- Odgovor: **ne** (`satisfied: false`)
- Podciljevi: osnovni podaci ✓, klasifikacija ✓, nedostaju obavezni dokumenti ✗, otvoreni zadaci ✗

### Varijanta — spreman predmet
Nakon [scenarija 9](#9-spreman-za-pregled-l5) → isti upit → **da**.

---

## 19. Backward chaining — glavna pravna radnja

**Cilj:** Rekurzivni BC upit + forward pravila `Main legal action blocked/reachable`.

### Koraci (Demo predmet)
1. U pregledu rezultata: **Glavna pravna radnja → Podnošenje tužbe — blokirana**
2. BC panel → **Da li je moguća glavna pravna radnja?**

### Očekivano
- Odgovor: **ne**
- Podciljevi po lancu: utvrđivanje osnova, slanje opomene, podnošenje tužbe
- Blokeri: nedostaje **opomena pre tužbe** (i eventualno drugi dokumenti)

### Varijanta — deblokiranje
U formi označi **Opomena pre tužbe** ✓ + sve obavezne dokumente → **Sačuvaj izmene** → BC upit → lanac se zatvara ka „moguća".

Graf preduslova (naplata):
```
PODNOŠENJE TUŽBE ⇐ opomena
                 ⇐ SLANJE OPOMENE
                      ⇐ UTVRĐIVANJE OSNOVA ⇐ ugovor + faktura
```

---

## Mapa pravila → scenarij

| Grupa | Broj pravila | Scenarij |
|-------|--------------|----------|
| L1 validacija | 5 | 2 |
| L2 klasifikacija | 5 | 1, 3–7 |
| L3 checklist (template) | 11 | 1, 3–5 |
| L4 accumulate | 5 | 1, 3, 5, 11 |
| L5 status | 8 | 1–11, 16 |
| DATES | 3 | 1, 4, 8 |
| CEP | 4 | 1, 12–15 |
| Procedure (forward + BC) | 2 + upiti | 1, 18, 19 |
| Audit | — | 17 |

---

## Šta se ne može demonstrirati kroz pravila

Ova polja postoje u formi, ali **nemaju pravila** u engine-u:

- `claimAmount` (iznos potraživanja) — samo se čuva
- datumi: `OBLIGATION_DATE`, `LAST_ACTION_DATE`, `OPENED_DATE`
- dokumenti: `ID_CARD`, `REGISTRATION_EXTRACT`, `OTHER` (van L3 checkliste)

---

## Rešavanje problema

| Problem | Rešenje |
|---------|---------|
| CEP alarm se ne pojavi posle +7 | Klikni **Ponovi procenu** posle pomeranja sata |
| Status ostaje „Čeka dopunu" posle CEP | Posle +7 treba „Zahteva pažnju" (p3); proveri da nisi ažurirao predmet u međuvremenu |
| Ne mogu „Spreman za pregled" | Svi obavezni **i** očekivani dokumenti ✓; **bez datuma** |
| L4 opterećenje se ne vidi | Potrebna su **2 predmeta** u istom statusu |
| BC panel prazan | Prvo izaberi predmet iz liste levo |
| Backend greška | `docker compose up -d` + `./mvnw spring-boot:run` u `back-end/` |
