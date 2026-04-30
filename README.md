# SBNZ – Predlog projekta

## Ekspertski sistem za proveru kompletnosti i organizaciju pravnih predmeta

## Član tima

- Dejan Lisica `SV49/2022`

---

## 1. Motivacija

Pravne kancelarije i pravne službe svakodnevno obrađuju predmete koji se razlikuju po vrsti spora, potrebnoj dokumentaciji, relevantnim datumima i narednim administrativnim koracima. Već pri prijemu predmeta potrebno je proveriti da li su uneti osnovni podaci, da li nedostaju ključni dokumenti, da li postoje datumi koje korisnik treba ručno da proveri i da li predmet može biti prosleđen pravniku na inicijalni pregled.

Cilj projekta je razvoj ekspertskog sistema koji ne donosi pravno mišljenje i ne zamenjuje pravnika, već služi kao pomoćni alat za proveru kompletnosti i organizaciju pravnog predmeta. Sistem formalizuje deo kancelarijskog i domenskog znanja kroz transparentna pravila: proverava podatke i dokumentaciju, označava datume koje treba proveriti, generiše administrativne zadatke i određuje status predmeta u toku inicijalne obrade.

---

## 2. Pregled problema

### 2.1 Problem koji rešavamo

U početnoj obradi pravnog predmeta potrebno je povezati više različitih informacija: ko su strane u predmetu, koji je tip predmeta, koji dokumenti su dostavljeni, koji datumi su relevantni i koje radnje su već preduzete. Ako se ovi elementi proveravaju ručno, može doći do sporije obrade, neujednačenog evidentiranja predmeta, previdа nedostajućih dokumenata ili kašnjenja u prosleđivanju predmeta na pregled.

U ovom projektu sistem neće obuhvatati sve pravne oblasti niti će donositi konačne pravne zaključke. Fokus je na administrativnoj trijaži i proveri kompletnosti za ograničen skup čestih tipova predmeta:

- naplata potraživanja,
- naknada štete,
- imovinsko-pravni predmeti.

Sistem treba da odgovori na pitanja:

- da li su uneti osnovni podaci o predmetu;
- da li je tip predmeta poznat ili ga je moguće preliminarno klasifikovati;
- koji obavezni ili očekivani dokumenti nedostaju za dati tip predmeta;
- da li postoje datumi koje treba ručno proveriti;
- koji administrativni zadaci treba da se kreiraju;
- da li je predmet spreman za inicijalni pregled od strane pravnika.

### 2.2 Postojeći pristupi i njihovi nedostaci

Postojeća softverska rešenja u pravnom domenu uglavnom su legal case-management sistemi. Ona su usmerena na evidenciju predmeta, čuvanje dokumenata, kalendare, zadatke, komunikaciju, automatizovane tokove rada i generisanje dokumenata. Takvi sistemi su korisni za organizaciju rada, ali često ne prikazuju eksplicitno pravila na osnovu kojih je predmet označen kao nepotpun, spreman za pregled ili rizičan zbog neaktivnosti.

U praksi to znači da korisnik i dalje mora ručno da proverava da li su za konkretan tip predmeta uneti svi potrebni podaci, da li nedostaju dokumenti, da li postoji datum koji zahteva pažnju i koji zadatak treba sledeći kreirati. Posebno je važno da sistem ne tvrdi da je rok definitivno istekao ili da postoji pravni osnov, već da označi situacije koje zahtevaju stručnu proveru.

### 2.3 Prednost predloženog rešenja

Predloženo rešenje koristi sistem baziran na znanju koji na osnovu unetih činjenica izvodi administrativne zaključke o stanju predmeta. Za razliku od obične evidencije, sistem kroz pravila proverava kompletnost podataka i dokumentacije, generiše zadatke, označava potencijalno važne datume i određuje status predmeta u inicijalnoj obradi.

Odluke sistema su transparentne: korisnik može da vidi koja su pravila aktivirana i koje činjenice su dovele do zaključka. Sistem uključuje višeslojno forward-chaining rezonovanje, `accumulate` funkcije za brojanje nedostajućih elemenata, CEP za praćenje vremenski zavisnih događaja, templejte za generisanje sličnih pravila po tipu predmeta i backward chaining za proveru ciljnih pitanja, kao što su: „Da li je predmet spreman za inicijalni pregled?” i „Šta nedostaje da bi predmet bio kompletan?”

---

## 3. Metodologija rada

### 3.1 Ulazi u sistem (input)

Sistem koristi sledeće ulaze:

1. Osnovni podaci o predmetu:
   - naziv predmeta,
   - kratak opis situacije,
   - tip stranke,
   - druga strana u predmetu,
   - kontakt podaci stranke,
   - odgovorno lice ili korisnik koji obrađuje predmet.

2. Opcioni početni tip predmeta, ukoliko ga korisnik zna:
   - naplata potraživanja,
   - naknada štete,
   - imovinsko-pravni predmet.

3. Činjenice na osnovu kojih sistem može preliminarno da klasifikuje predmet:
   - postoji potraživanje ili dug,
   - postoji šteta,
   - postoji ugovor,
   - postoji faktura ili račun,
   - postoji nepokretnost,
   - postoje katastarski podaci,
   - postoji odluka organa, rešenje ili drugi akt.

4. Relevantni datumi:
   - datum nastanka obaveze,
   - datum dospelosti potraživanja,
   - datum nastanka štete,
   - datum prijema odluke ili akta,
   - datum poslednje preduzete radnje,
   - datum otvaranja predmeta.

5. Podaci o dokumentaciji:
   - da li postoji ugovor,
   - da li postoji faktura ili račun,
   - da li postoji opomena,
   - da li postoji dokaz o uplati ili izostanku uplate,
   - da li postoji dokaz o izvršenoj usluzi ili isporuci,
   - da li postoji dokaz o šteti,
   - da li postoji zapisnik,
   - da li postoji izvod iz katastra,
   - da li postoje fotografije, dopisi ili drugi prilozi.

6. Status predmeta i događaji:
   - nov predmet,
   - predmet u dopuni,
   - čeka dokumentaciju od stranke,
   - spreman za pregled,
   - arhiviran predmet,
   - predmet otvoren,
   - dokument dodat,
   - zadatak kreiran,
   - zadatak završen,
   - predmet ažuriran,
   - predmet nije ažuriran određeni broj dana.

### 3.2 Izlazi iz sistema (output)

Sistem emituje:

- preliminarnu klasifikaciju predmeta po tipu;
- listu nedostajućih osnovnih podataka;
- status dokumentacije:
  - `COMPLETE`,
  - `PARTIAL`,
  - `INCOMPLETE`;
- listu nedostajućih obaveznih i očekivanih dokumenata;
- oznake i upozorenja:
  - potreban pregled datuma,
  - predmet nije ažuriran duže od definisanog perioda,
  - predmet ima više nedostajućih elemenata,
  - predmet treba proslediti pravniku;
- administrativne zadatke:
  - zatražiti dokument od stranke,
  - proveriti da li postoji opomena,
  - proveriti relevantan datum,
  - proslediti predmet pravniku na inicijalni pregled;
- status predmeta u inicijalnoj obradi:
  - `INCOMPLETE`,
  - `WAITING_FOR_CLIENT`,
  - `NEEDS_DATE_CHECK`,
  - `READY_FOR_REVIEW`,
  - `NEEDS_ATTENTION`,
  - `ARCHIVED`;
- objašnjenje odluke kroz prikaz aktiviranih pravila;
- zbirne pokazatelje:
  - broj nedostajućih dokumenata po predmetu,
  - broj predmeta koji čekaju dopunu dokumentacije,
  - broj predmeta spremnih za inicijalni pregled.

### 3.3 Baza znanja i način popunjavanja

Baza znanja je organizovana kroz:

- statička pravila za validaciju osnovnih podataka (`validation_rules.drl`),
- statička pravila za određivanje statusa predmeta (`case_status_rules.drl`),
- generisana pravila za proveru dokumentacije (`generated_document_rules.drl`),
- CEP pravila za neaktivnost i dopunu dokumentacije (`cep_rules.drl`),
- templejte za generisanje pravila po tipu predmeta, tipu stranke i tipu dokumenta (`document_checklist_template.drt`),
- backward-chaining upite za proveru spremnosti predmeta (`queries.drl`).

Instanciranje templejt pravila:

- Templejt fajl definiše šablon pravila sa promenljivim poljima: `caseType`, `partyType`, `documentType`, `required`, `taskIfMissing`.
- Osnovna dimenzija konfiguracije je `caseType`, ali pojedina pravila mogu dodatno zavisiti od tipa stranke (`partyType`).
- Za dokumente koji su očekivani bez obzira na tip stranke koristi se vrednost `ANY`.
- Prilikom pokretanja aplikacije, sistem prolazi kroz konfiguraciju dokumenata za svaki tip predmeta.
- Za svaku kombinaciju tipa predmeta, tipa stranke i dokumenta generiše se konkretno DRL pravilo.
- Na taj način se dobija veći broj sličnih pravila bez ručnog pisanja svakog pojedinačno.

Popunjavanje znanja:

1. korisnik unosi ili ažurira predmet,
2. sistem ubacuje činjenice u Working Memory,
3. pravila izvode nove činjenice, kao što su `MissingRequiredData`, `CaseClassification`, `MissingRequiredDocument`, `SuggestedTask` i `CaseStatus`,
4. pri svakoj promeni statusa ili kreiranju zadatka čuva se audit zapis,
5. backward-chaining upiti omogućavaju proveru ciljnih pitanja, npr. da li je predmet spreman za inicijalni pregled.

Interakcije zasnovane na znanju:

- dodavanje dokumenta uklanja upozorenje o nedostajućem dokumentu;
- ako predmet dugo nije ažuriran, CEP pravila generišu alarm;
- ako su osnovni podaci i obavezna dokumentacija kompletni, sistem menja status u `READY_FOR_REVIEW`;
- ako postoje nedostajući dokumenti, sistem kreira zadatke za dopunu dokumentacije.

---

## 4. Entiteti domena

### 4.1 Ključni entiteti (fakti)

- `LegalCase` – pravni predmet u sistemu.
- `Party` – stranka ili druga strana u predmetu.
- `Document` – dokument koji je dostavljen ili nedostaje.
- `DateFact` – relevantan datum za predmet.
- `CaseClassification` – preliminarna klasifikacija predmeta.
- `MissingRequiredData` – nedostajući osnovni podatak.
- `MissingRequiredDocument` – nedostajući obavezni dokument.
- `MissingExpectedDocument` – nedostajući očekivani, ali ne nužno obavezni dokument.
- `DocumentationChecklistStatus` – status dokumentacije.
- `ImportantDateNeedsCheck` – oznaka da određeni datum treba proveriti.
- `SuggestedTask` – administrativni zadatak koji sistem predlaže.
- `CaseStatusCandidate` – kandidat za status predmeta.
- `CaseStatus` – konačni status predmeta u inicijalnoj obradi.
- `CaseEvent` – događaj nad predmetom, koristi se za CEP.
- `AuditRecord` – zapis o aktiviranom pravilu ili promeni statusa.

### 4.2 Tipovi predmeta, tipovi stranke i dokumenti

Konfiguracija dokumenata po tipu predmeta koristi se za generisanje templejt pravila. Osnovna dimenzija konfiguracije je `caseType`, ali pojedina pravila mogu dodatno zavisiti od tipa stranke (`partyType`). Za dokumente koji su očekivani bez obzira na tip stranke koristi se vrednost `ANY`.

Konfiguracija dokumenata po tipu predmeta:

| Tip predmeta | Tip stranke | Dokument | Obavezan | Zadatak ako nedostaje |
|---|---|---|---|---|
| `DEBT_COLLECTION` | `ANY` | `CONTRACT` | Da | `REQUEST_CONTRACT` |
| `DEBT_COLLECTION` | `ANY` | `INVOICE` | Da | `REQUEST_INVOICE` |
| `DEBT_COLLECTION` | `ANY` | `SERVICE_PROOF` | Da | `REQUEST_SERVICE_PROOF` |
| `DEBT_COLLECTION` | `ANY` | `PRE_LAWSUIT_NOTICE` | Ne | `CHECK_IF_NOTICE_EXISTS` |
| `DEBT_COLLECTION` | `LEGAL_ENTITY` | `COMPANY_REGISTRATION_DATA` | Ne | `CHECK_COMPANY_DATA` |
| `DEBT_COLLECTION` | `NATURAL_PERSON` | `PERSONAL_DATA` | Ne | `CHECK_PERSONAL_DATA` |
| `DAMAGES` | `ANY` | `DAMAGE_PROOF` | Da | `REQUEST_DAMAGE_PROOF` |
| `DAMAGES` | `ANY` | `DAMAGE_REPORT` | Ne | `CHECK_IF_DAMAGE_REPORT_EXISTS` |
| `PROPERTY` | `ANY` | `CADASTRE_EXTRACT` | Da | `REQUEST_CADASTRE_EXTRACT` |
| `PROPERTY` | `ANY` | `OWNERSHIP_DOCUMENT` | Da | `REQUEST_OWNERSHIP_DOCUMENT` |
| `PROPERTY` | `PUBLIC_ENTITY` | `AUTHORIZATION_OR_DECISION` | Ne | `CHECK_AUTHORIZATION_DOCUMENT` |

Ova konfiguracija se koristi za generisanje templejt pravila. Ako se doda novi tip predmeta ili novi dokument, nije potrebno ručno pisati sva pravila, već se dopunjava konfiguraciona tabela.

---

## 5. Struktura pravila i nivoi rezonovanja

### 5.1 L1 pravila za validaciju osnovnih podataka

Ova pravila proveravaju da li predmet ima minimalne podatke potrebne za dalju obradu.

| Pravilo | Uslov | Ishod |
|---|---|---|
| Missing case name | predmet nema naziv | `MissingRequiredData(CASE_NAME)` |
| Missing opposing party | predmet nema unetu drugu stranu | `MissingRequiredData(OPPOSING_PARTY)` |
| Missing description | predmet nema opis situacije | `MissingRequiredData(CASE_DESCRIPTION)` |
| Missing contact | predmet nema kontakt podatke stranke | `MissingRequiredData(CONTACT)` |
| Basic data complete | ne postoji `MissingRequiredData` za osnovna polja | `BasicDataStatus(COMPLETE)` |

### 5.2 L2 pravila za preliminarnu klasifikaciju predmeta

Ova pravila određuju najverovatniji tip predmeta na osnovu unetih indikatora. Klasifikacija služi za izbor odgovarajuće checklist-e dokumenata, a ne za donošenje pravnog mišljenja.

| Pravilo | Uslov | Ishod |
|---|---|---|
| Classify debt collection | postoji potraživanje ili dug + postoji faktura ili datum dospelosti | `CaseClassification(DEBT_COLLECTION, HIGH)` |
| Classify damages | postoji šteta + postoji dokaz o šteti ili zapisnik | `CaseClassification(DAMAGES, HIGH)` |
| Classify property | postoji nepokretnost ili katastarski podaci | `CaseClassification(PROPERTY, HIGH)` |
| Unknown case type | nijedno pravilo klasifikacije nije aktivirano | `CaseClassification(UNKNOWN, LOW)` + `SuggestedTask(MANUAL_CASE_TYPE_REVIEW)` |

### 5.3 L3 pravila za proveru dokumentacije

Ova pravila se generišu iz templejta na osnovu konfiguracije dokumenata po tipu predmeta i tipu stranke.

Zajednička logika za svako pravilo:

- aktivira se kada postoji `CaseClassification(caseType)`;
- proverava se da li konfiguracija dokumenta važi za dati `caseType` i `partyType`;
- ako je u konfiguraciji `partyType = ANY`, pravilo važi za sve tipove stranaka;
- proverava se da li za taj tip predmeta postoji očekivani dokument;
- ako dokument nedostaje, ubacuje se `MissingRequiredDocument` ili `MissingExpectedDocument`;
- kreira se odgovarajući `SuggestedTask`.

Primeri pravila:

| Pravilo | Uslov | Ishod |
|---|---|---|
| Debt collection missing contract | `DEBT_COLLECTION` + nema `CONTRACT` | `MissingRequiredDocument(CONTRACT)` + `SuggestedTask(REQUEST_CONTRACT)` |
| Debt collection missing invoice | `DEBT_COLLECTION` + nema `INVOICE` | `MissingRequiredDocument(INVOICE)` + `SuggestedTask(REQUEST_INVOICE)` |
| Debt collection missing service proof | `DEBT_COLLECTION` + nema `SERVICE_PROOF` | `MissingRequiredDocument(SERVICE_PROOF)` + `SuggestedTask(REQUEST_SERVICE_PROOF)` |
| Debt collection missing notice | `DEBT_COLLECTION` + nema `PRE_LAWSUIT_NOTICE` | `MissingExpectedDocument(PRE_LAWSUIT_NOTICE)` + `SuggestedTask(CHECK_IF_NOTICE_EXISTS)` |
| Debt collection company data check | `DEBT_COLLECTION` + `LEGAL_ENTITY` + nema `COMPANY_REGISTRATION_DATA` | `MissingExpectedDocument(COMPANY_REGISTRATION_DATA)` + `SuggestedTask(CHECK_COMPANY_DATA)` |
| Debt collection personal data check | `DEBT_COLLECTION` + `NATURAL_PERSON` + nema `PERSONAL_DATA` | `MissingExpectedDocument(PERSONAL_DATA)` + `SuggestedTask(CHECK_PERSONAL_DATA)` |
| Damages missing damage proof | `DAMAGES` + nema `DAMAGE_PROOF` | `MissingRequiredDocument(DAMAGE_PROOF)` + `SuggestedTask(REQUEST_DAMAGE_PROOF)` |
| Property missing cadastre extract | `PROPERTY` + nema `CADASTRE_EXTRACT` | `MissingRequiredDocument(CADASTRE_EXTRACT)` + `SuggestedTask(REQUEST_CADASTRE_EXTRACT)` |

### 5.4 L4 accumulate pravila za status kompletnosti

`accumulate` se koristi za brojanje nedostajućih elemenata i izvođenje statusa dokumentacije.

| Pravilo | Uslov | Ishod |
|---|---|---|
| Documentation complete | broj `MissingRequiredDocument` je 0 | `DocumentationChecklistStatus(COMPLETE)` |
| Documentation partial | broj `MissingRequiredDocument` je 1 ili 2 | `DocumentationChecklistStatus(PARTIAL)` |
| Documentation incomplete | broj `MissingRequiredDocument` je veći od 2 | `DocumentationChecklistStatus(INCOMPLETE)` |

Primer Drools izraza:

```java
$count : Number() from accumulate(
    MissingRequiredDocument(caseId == $caseId),
    count(1)
)
```

Drugo accumulate pravilo koristi se za pregled opterećenja:

| Pravilo | Uslov | Ishod |
|---|---|---|
| Many cases waiting for client | broj predmeta sa statusom `WAITING_FOR_CLIENT` veći od praga | `OfficeLoadWarning(MANY_CASES_WAITING_FOR_DOCUMENTS)` |
| Many ready cases | broj predmeta sa statusom `READY_FOR_REVIEW` veći od praga | `OfficeLoadWarning(MANY_CASES_READY_FOR_REVIEW)` |

### 5.5 L5 pravila za određivanje statusa predmeta

Status predmeta se izvodi na osnovu prethodnih zaključaka: osnovnih podataka, dokumentacije, zadataka i datuma koji zahtevaju proveru. Pošto više pravila može istovremeno ukazivati na različita stanja predmeta, statusi imaju unapred definisan prioritet. Konačni status predmeta bira se kao najkritičniji status među svim aktivnim status kandidatima.

Prioritet statusa je definisan sledećim redosledom:

| Prioritet | Status | Značenje |
|---|---|---|
| 1 | `INCOMPLETE` | Nedostaju osnovni podaci bez kojih predmet ne može biti uredno evidentiran |
| 2 | `WAITING_FOR_CLIENT` | Nedostaju obavezni dokumenti koje treba zatražiti od stranke |
| 3 | `NEEDS_ATTENTION` | Postoji CEP alarm, duža neaktivnost ili otvorena situacija koja zahteva reakciju korisnika |
| 4 | `NEEDS_DATE_CHECK` | Postoji datum koji treba ručno proveriti |
| 5 | `READY_FOR_REVIEW` | Predmet ima osnovne podatke i obaveznu dokumentaciju za inicijalni pregled |
| 6 | `ARCHIVED` | Predmet je arhiviran i ne učestvuje u daljoj automatskoj proceni |

Pravila za status ne upisuju direktno konačni status, već najpre kreiraju kandidate za status. Zatim se posebnim pravilom bira status sa najvišim prioritetom.

| Pravilo | Uslov | Ishod |
|---|---|---|
| Candidate incomplete | postoji `MissingRequiredData` | `CaseStatusCandidate(INCOMPLETE, priority=1)` |
| Candidate waiting for client | postoji `MissingRequiredDocument` | `CaseStatusCandidate(WAITING_FOR_CLIENT, priority=2)` |
| Candidate needs attention | postoji `CepAlert` ili `CaseInactive` | `CaseStatusCandidate(NEEDS_ATTENTION, priority=3)` |
| Candidate needs date check | postoji `ImportantDateNeedsCheck` | `CaseStatusCandidate(NEEDS_DATE_CHECK, priority=4)` |
| Candidate ready for review | osnovni podaci kompletni + dokumentacija kompletna + nema blokirajućih zadataka | `CaseStatusCandidate(READY_FOR_REVIEW, priority=5)` |

Konačni status se određuje izborom kandidata sa najvišim prioritetom, odnosno najmanjom numeričkom vrednošću prioriteta.

Pseudo-pravilo za izbor konačnog statusa:

```java
$candidate : CaseStatusCandidate($p : priority)
not CaseStatusCandidate(priority < $p)
then
  insert(new CaseStatus($candidate.getStatus()));
```

Primer: ako predmet istovremeno ima `MissingRequiredData(CONTACT)` i `ImportantDateNeedsCheck(DUE_DATE)`, sistem neće postaviti status `NEEDS_DATE_CHECK`, već `INCOMPLETE`, jer nedostajući osnovni podaci imaju veći prioritet. Datum će i dalje ostati označen za proveru, ali neće biti glavni status predmeta.

### 5.6 CEP pravila za neaktivnost i dopunu dokumentacije

CEP se koristi za praćenje događaja nad predmetom kroz vreme. Pravila nisu samo logovanje aktivnosti, već direktno utiču na status predmeta i prioritet obrade.

| CEP pravilo | Uslov | Ishod |
|---|---|---|
| Missing document not added | kreiran zadatak za dokument + dokument nije dodat u 7 dana | CEP alarm + `CaseStatusCandidate(NEEDS_ATTENTION, priority=3)` |
| Case waiting too long | predmet je `WAITING_FOR_CLIENT` + nema ažuriranja 14 dana | CEP alarm + `SuggestedTask(CHECK_CASE_STATUS)` |
| Ready case not reviewed | predmet je `READY_FOR_REVIEW` + nije pregledan 7 dana | CEP alarm + `SuggestedTask(ASSIGN_REVIEW)` |

Primer CEP pravila:

```java
SuggestedTask(type == REQUEST_SERVICE_PROOF)
not DocumentAdded(type == SERVICE_PROOF) over window:time(7d)
```

Ishod:

```java
INSERT: CepAlert(type: DOCUMENT_NOT_ADDED)
INSERT: SuggestedTask(type: CHECK_DOCUMENT_REQUEST_STATUS)
INSERT: CaseStatusCandidate(status: NEEDS_ATTENTION, priority: 3)
```

### 5.7 Pravila za označavanje važnih datuma

Sistem ne računa konačne pravne rokove, već označava datume koje korisnik treba ručno da proveri.

| Pravilo | Uslov | Ishod |
|---|---|---|
| Decision date needs check | postoji datum prijema odluke ili akta | `ImportantDateNeedsCheck(DECISION_RECEIVED_DATE)` + `SuggestedTask(VERIFY_DATE_RELEVANCE)` |
| Due date needs check | postoji datum dospelosti potraživanja | `ImportantDateNeedsCheck(DUE_DATE)` + `SuggestedTask(VERIFY_DUE_DATE)` |
| Damage date needs check | postoji datum nastanka štete | `ImportantDateNeedsCheck(DAMAGE_DATE)` + `SuggestedTask(VERIFY_DAMAGE_DATE)` |
| Old case activity | poslednja radnja starija od praga | `CaseInactive(days=X)` + `SuggestedTask(CHECK_CASE_STATUS)` |

### 5.8 Backward-chaining upiti

Backward chaining se koristi za ciljna pitanja koja korisnik može da postavi sistemu.

| Upit | Svrha |
|---|---|
| `isCaseReadyForInitialReview(caseId)` | proverava da li je predmet spreman za inicijalni pregled |
| `whatIsMissingForReview(caseId)` | vraća šta nedostaje da bi predmet bio spreman |
| `hasBlockingTasks(caseId)` | proverava da li postoje otvoreni zadaci koji blokiraju pregled |

Primer ciljnog pitanja:

```text
Da li je predmet spreman za inicijalni pregled?
```

Sistem proverava:

- da li su osnovni podaci kompletni;
- da li je predmet klasifikovan;
- da li nema nedostajućih obaveznih dokumenata;
- da li nema otvorenih blokirajućih zadataka;
- da li su važni datumi označeni za proveru.

### 5.9 Sažetak ulančavanja kroz nivoe

U tipičnom toku rezonovanja:

1. L1 pravila proveravaju osnovne podatke i kreiraju `MissingRequiredData` ili `BasicDataStatus`.
2. L2 pravila preliminarno klasifikuju predmet.
3. L3 template pravila proveravaju dokumentaciju za dati tip predmeta i tip stranke.
4. L4 accumulate pravila broje nedostajuće dokumente i formiraju status dokumentacije.
5. L5 pravila kreiraju kandidate za status i biraju konačni status prema prioritetu.
6. CEP pravila prate neaktivnost i kašnjenje u dopuni dokumentacije.
7. Backward-chaining upiti odgovaraju na pitanja korisnika o spremnosti predmeta.

---

## 6. Konkretan primer rezonovanja

Scenario: predmet naplate potraživanja nije unapred klasifikovan.

1. Korisnik unosi novi predmet:
   `Claim(amount = 450000)`, `Party(type = LEGAL_ENTITY)`, `Document(INVOICE, true)`, `Document(CONTRACT, true)`,
   `Document(SERVICE_PROOF, false)`, `DateFact(DUE_DATE, "2024-03-15")`.

2. L1 pravila proveravaju osnovne podatke. Pošto su naziv predmeta, druga strana i opis uneti, sistem dodaje:
   `BasicDataStatus(COMPLETE)`.

3. L2 pravilo za klasifikaciju detektuje da postoje potraživanje, faktura i datum dospelosti, pa dodaje:
   `CaseClassification(DEBT_COLLECTION, HIGH)`.

4. L3 template pravila proveravaju checklist-u za `DEBT_COLLECTION` i `partyType = LEGAL_ENTITY`. Sistem pronalazi da nedostaje dokaz o izvršenoj usluzi i dodaje:
   `MissingRequiredDocument(SERVICE_PROOF)` i `SuggestedTask(REQUEST_SERVICE_PROOF)`.

5. L3 pravilo proverava očekivanu opomenu. Pošto opomena nije dostavljena, sistem dodaje:
   `MissingExpectedDocument(PRE_LAWSUIT_NOTICE)` i `SuggestedTask(CHECK_IF_NOTICE_EXISTS)`.

6. L3 pravilo proverava dokumente specifične za pravno lice. Ako nisu uneti podaci o firmi, sistem dodaje:
   `MissingExpectedDocument(COMPANY_REGISTRATION_DATA)` i `SuggestedTask(CHECK_COMPANY_DATA)`.

7. L4 accumulate pravilo broji nedostajuće obavezne dokumente. Broj je 1, pa sistem dodaje:
   `DocumentationChecklistStatus(PARTIAL)`.

8. Pravilo za važne datume detektuje datum dospelosti i dodaje:
   `ImportantDateNeedsCheck(DUE_DATE)` i `SuggestedTask(VERIFY_DUE_DATE)`.

9. L5 pravila kreiraju kandidate za status:
   - zbog nedostajućeg obaveznog dokumenta: `CaseStatusCandidate(WAITING_FOR_CLIENT, priority=2)`,
   - zbog važnog datuma: `CaseStatusCandidate(NEEDS_DATE_CHECK, priority=4)`.

10. Pravilo za izbor statusa bira status sa najvišim prioritetom, odnosno najmanjom numeričkom vrednošću prioriteta:
    `CaseStatus(WAITING_FOR_CLIENT)`.

11. Korisniku se prikazuje rezultat:
    - predmet je preliminarno klasifikovan kao naplata potraživanja;
    - osnovni podaci su kompletni;
    - dokumentacija je delimično kompletna;
    - nedostaje dokaz o izvršenoj usluzi;
    - treba proveriti da li postoji opomena;
    - treba proveriti relevantnost datuma dospelosti;
    - predmet čeka dopunu dokumentacije.

12. Ako dokument `SERVICE_PROOF` ne bude dodat u narednih 7 dana, CEP pravilo generiše:
    `CepAlert(DOCUMENT_NOT_ADDED)` i `CaseStatusCandidate(NEEDS_ATTENTION, priority=3)`.

13. Korisnik može da postavi backward-chaining upit:
    `isCaseReadyForInitialReview(caseId)`.

14. Sistem vraća:
    `false`, jer postoji nedostajući obavezan dokument i otvoren zadatak za dopunu dokumentacije.

---

## 7. Primeri kompleksnih pravila i korišćenja baze znanja

### Primer 1 – Template pravilo za dokumentaciju

Uslov:

- predmet je klasifikovan kao `DEBT_COLLECTION`,
- tip stranke je `LEGAL_ENTITY`,
- dokument `SERVICE_PROOF` nije dostavljen,
- pravilo je generisano iz konfiguracije dokumenata.

Zaključak:

- dodaje se `MissingRequiredDocument(SERVICE_PROOF)`,
- kreira se `SuggestedTask(REQUEST_SERVICE_PROOF)`.

Kompleksnost:

- isto pravilo se ne piše ručno za svaki dokument, već se generiše iz templejta na osnovu tipa predmeta, tipa stranke i konfiguracije dokumenata.

### Primer 2 – Accumulate pravilo za status dokumentacije

Uslov:

- sistem broji sve `MissingRequiredDocument` činjenice za jedan predmet.

Zaključak:

- ako je broj 0, dokumentacija je kompletna;
- ako je broj 1 ili 2, dokumentacija je delimično kompletna;
- ako je broj veći od 2, dokumentacija je nekompletna.

Kompleksnost:

- status ne zavisi od jednog pravila, već od zbirnog stanja više činjenica u Working Memory.

### Primer 3 – CEP pravilo za neaktivnost predmeta

Uslov:

- predmet ima status `WAITING_FOR_CLIENT`,
- u periodu od 14 dana nije dodat novi dokument niti je predmet ažuriran.

Zaključak:

- generiše se `CepAlert(CASE_INACTIVE)`,
- dodaje se `SuggestedTask(CHECK_CASE_STATUS)`,
- predmet dobija status kandidata `NEEDS_ATTENTION`.

Kompleksnost:

- pravilo koristi vremenski prozor i događaje nad predmetom, a ne samo trenutno stanje.

### Primer 4 – Prioritet statusa

Uslov:

- predmet istovremeno ima `MissingRequiredData(CONTACT)` i `ImportantDateNeedsCheck(DUE_DATE)`.

Zaključak:

- sistem bira `INCOMPLETE` kao konačni status, jer nedostajući osnovni podaci imaju veći prioritet od potrebe za proverom datuma.

Kompleksnost:

- sistem rešava konflikt između više mogućih statusa na osnovu definisanog prioriteta.

### Primer 5 – Backward-chaining upit

Pitanje:

```text
Da li je predmet spreman za inicijalni pregled?
```

Sistem proverava:

- da li su osnovni podaci kompletni;
- da li postoji klasifikacija predmeta;
- da li nema nedostajućih obaveznih dokumenata;
- da li nema otvorenih blokirajućih zadataka.

Zaključak:

- ako su svi uslovi ispunjeni, predmet je spreman za pregled;
- ako nisu, sistem vraća šta nedostaje.

---

## 8. Tehnička arhitektura

| Komponenta | Tehnologija | Uloga |
|---|---|---|
| Rule Engine | Drools | Forward/backward chaining, `accumulate`, CEP i templejti |
| Backend | Spring Boot | REST API, integracija sa Drools `KieSession` i obrada pravnih predmeta |
| Frontend | Angular | Unos podataka o predmetu, prikaz statusa, zadataka, upozorenja i aktiviranih pravila |
| Baza podataka | PostgreSQL | Čuvanje predmeta, dokumenata, događaja, zadataka i istorije odluka |
| CEP | Drools Fusion | Praćenje neaktivnosti, dopune dokumentacije i vremenski zavisnih događaja nad predmetom |
| Template mehanizam | Drools Rule Templates | Generisanje sličnih pravila po tipu predmeta, tipu stranke i tipu dokumenta |
| Audit log | PostgreSQL / Backend servis | Čuvanje aktiviranih pravila, objašnjenja odluka i promena statusa predmeta |

---

## 9. Ograničenja sistema

Sistem ne daje pravne savete, ne donosi konačno pravno mišljenje i ne odlučuje o tome da li treba pokrenuti postupak. Njegova uloga je ograničena na administrativnu trijažu, proveru kompletnosti podataka i dokumentacije, označavanje datuma koji zahtevaju proveru i generisanje zadataka za korisnika.

Konačnu procenu predmeta, proveru rokova i odluku o daljem postupanju donosi ovlašćeno stručno lice. Sistem je zamišljen kao pomoćni alat koji smanjuje mogućnost administrativnih propusta i omogućava konzistentniju inicijalnu obradu predmeta.

---

## 10. Reference

[1] Clio — Legal Case Management Software  
[2] MyCase — Legal Case Management and Legal Calendaring Software
