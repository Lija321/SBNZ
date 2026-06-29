# SBNZ – Specifikacija implementacije

## Ekspertski sistem za proveru kompletnosti i organizaciju pravnih predmeta

Dokument prati strukturu [README.md](README.md) (predlog projekta), ali opisuje **trenutno implementirano stanje** u repozitorijumu — backend, frontend, baza, pravila i API.

Povezani dokumenti: [demo.md](demo.md) (scenariji demonstracije).

## Član tima

- Dejan Lisica `SV49/2022`

---

## 1. Motivacija

Pravne kancelarije i pravne službe svakodnevno obrađuju predmete koji se razlikuju po vrsti spora, potrebnoj dokumentaciji, relevantnim datumima i narednim administrativnim koracima. Već pri prijemu predmeta potrebno je proveriti da li su uneti osnovni podaci, da li nedostaju ključni dokumenti, da li postoje datumi koje korisnik treba ručno da proveri i da li predmet može biti prosleđen pravniku na inicijalni pregled.

**Implementirani sistem** ne donosi pravno mišljenje i ne zamenjuje pravnika. Služi kao pomoćni alat za proveru kompletnosti i organizaciju predmeta kroz transparentna Drools pravila. Korisnik unosi podatke u **Angular** web aplikaciji; **Spring Boot** backend evaluira predmet i vraća status, zadatke, alarme i audit.

---

## 2. Pregled problema

### 2.1 Problem koji rešavamo

U početnoj obradi pravnog predmeta potrebno je povezati više različitih informacija: ko su strane u predmetu, koji je tip predmeta, koji dokumenti su dostavljeni, koji datumi su relevantni i koje radnje su već preduzete.

Sistem pokriva administrativnu trijažu za tri tipa predmeta:

- naplata potraživanja (`DEBT_COLLECTION`),
- naknada štete (`DAMAGES`),
- imovinsko-pravni predmeti (`PROPERTY`).

Sistem odgovara na pitanja:

- da li su uneti osnovni podaci o predmetu;
- da li je tip predmeta poznat ili ga je moguće preliminarno klasifikovati;
- koji obavezni ili očekivani dokumenti nedostaju;
- da li postoje datumi koje treba ručno proveriti;
- koji administrativni zadaci treba da se kreiraju;
- da li je predmet spreman za inicijalni pregled;
- da li je glavna pravna radnja blokirana nedostajućim preduslovima (backward chaining).

### 2.2 Postojeći pristupi i prednost rešenja

Za razliku od obične case-management evidencije, sistem eksplicitno prikazuje **aktivirana pravila**, **kandidate za status** i **audit log**. CEP prati vremenske događaje (zahtev za dokument, dopuna, neaktivnost) preko simulacionog sata.

---

## 3. Metodologija rada

### 3.1 Ulazi u sistem (input)

Ulazi se unose kroz **formu predmeta** (`CaseFormComponent`) i mapiraju na `CreateCaseRequest` / `UpdateCaseRequest`.

| Kategorija | Polja u formi | Backend / pravila |
|------------|---------------|-------------------|
| Osnovni podaci | naziv, opis, odgovorno lice | L1 |
| Stranke | tip klijenta, ime, kontakt; ime protivnika | L1 (kontakt, protivnik); L3 (tip stranke) |
| Početni tip | dropdown (opciono) | L2 salience 100 |
| Indikatori L2 | 7 checkbox-ova | L2 klasifikacija |
| Iznos potraživanja | `claimAmount` (opciono) | **Samo perzistencija** — nema pravila |
| Datumi | 6 tipova datuma | 3 tipa imaju pravila (vidi §5.7) |
| Dokumenti | 14 checkbox-ova | L3 za 11 tipova iz checkliste; ostali samo enum |

**Napomena o formi:** tip protivnika se u UI uvek šalje kao `LEGAL_ENTITY`; backend podržava sve `PartyType` vrednosti.

**Datumi u modelu:**

| DateType | U formi | Pravila |
|----------|---------|---------|
| `DUE_DATE` | Da | Da (L2, DATES) |
| `DAMAGE_DATE` | Da | Da (DATES) |
| `DECISION_RECEIVED_DATE` | Da | Da (DATES) |
| `OBLIGATION_DATE` | Da | Ne |
| `LAST_ACTION_DATE` | Da | Ne |
| `OPENED_DATE` | Da | Ne (odvojeno od `LegalCase.openedAt`) |

### 3.2 Izlazi iz sistema (output)

Izlazi se vraćaju u **`CaseReport`** (evaluacija) i prikazuju u **`CaseResultsComponent`**.

| Izlaz | Polje u CaseReport | Gde se vidi u UI |
|-------|-------------------|------------------|
| Klasifikacija | `classification`, `summary.classification` | Overview |
| Nedostajući osnovni podaci | `missingFields` | Predložene akcije |
| Status dokumentacije | `documentationChecklistStatus`, `summary.documentation` | Overview |
| Nedostajući obavezni / očekivani docs | `missingRequiredDocuments`, `missingExpectedDocuments` | Za pažnju / akcije |
| Datumi za proveru | `importantDatesToCheck` | Predložene akcije |
| Zadaci | `suggestedTasks` | Predložene akcije |
| Status predmeta | `summary.status` | Overview, sidebar |
| Kandidati za status | `statusCandidates` | Detalji rezonovanja |
| CEP alarmi | `cepAlerts` | Za pažnju |
| Neaktivnost | `caseInactive` | Za pažnju |
| Opterećenje kancelarije | `officeLoadWarnings` | Za pažnju |
| Glavna radnja | `mainActionGoal`, `mainActionBlocked` | Overview |
| Aktivirana pravila | `ruleFirings` | Transparentnost |
| Audit | `GET /audit` | Transparentnost |
| BC izveštaj | `BackwardChainingReport` | BC panel (na zahtev) |

**Ne prikazuje se u UI** (iako postoji u API-ju): `parties`, detalji `legalCase`, `evaluatedAt`, `claimAmount`.

### 3.3 Baza znanja i način popunjavanja

Baza znanja je organizovana kroz fajlove u `back-end/src/main/resources/rules/`:

| Fajl | Uloga |
|------|-------|
| `validation_rules.drl` | L1 |
| `classification_rules.drl` | L2 |
| `document_checklist_template.drt` + `document_checklist_data.xlsx` | L3 (11 generisanih pravila) |
| `accumulate_rules.drl` | L4 |
| `date_rules.drl` | Datumi za proveru |
| `cep_rules.drl` | CEP (4 pravila) |
| `case_status_rules.drl` | L5 |
| `procedure_rules.drl` | Forward + BC upit `isStepBlocked` |
| `queries.drl` | Backward-chaining upiti |

**Kompajliranje L3:** pri pokretanju aplikacije `DroolsConfig` koristi `ExternalSpreadsheetCompiler` — red u xlsx = jedno generisano pravilo. Izmena checkliste = izmena xlsx, bez Java koda.

**KieSession:** `STATEFUL`, `STREAM`, **pseudo-clock** (simulaciono vreme iz `SimulatedClockService`).

**Tok evaluacije:**
1. Replay CEP događaja iz tabele `case_timeline_event`.
2. Insert ulaznih činjenica + statičkog BC grafa + peer statusa drugih predmeta.
3. `fireAllRules()`.
4. Agregacija u `CaseReport`.

**Interakcije:**
- Dodavanje dokumenta pri update-u → `DocumentAddedEvent` → može ukloniti CEP alarm pri sledećoj evaluaciji.
- Novi otvoreni `REQUEST_*` zadatak → `TaskCreatedEvent` → hrani CEP „dokument nije dodat".
- Zadaci **nemaju** API za ručno zatvaranje — nestaju kad procena više ne generiše uslov.
- BC upiti se izvršavaju **na zahtev** (`BackwardChainingService`), ne u svakom `fireAllRules`.

---

## 4. Entiteti domena

### 4.1 Ključni entiteti (fakti)

Implementirani u `com.sbnz.legal.domain` — ubacuju se u Working Memory ili se izvode pravilima.

**Ulazni (iz baze / forme):**
- `LegalCase`, `Party`, `Document`, `DateFact`, `CaseIndicator` (uključujući `claimAmount`)

**Izvodeni (pravila):**
- `MissingRequiredData`, `BasicDataStatus`
- `CaseClassification`
- `MissingRequiredDocument`, `MissingExpectedDocument`
- `DocumentationChecklistStatus`
- `ImportantDateNeedsCheck`
- `SuggestedTask`
- `CaseStatusCandidate`, `CaseStatus`
- `CepAlert`, `CaseInactive`
- `OfficeLoadWarning`
- `MainActionAssessment`

**Statičko znanje (BC):**
- `ProcedureGoal`, `StepRequiresStep`, `StepRequiresDocument` — iz `ProcedurePrerequisiteRegistry`

**CEP događaji:**
- `TaskCreatedEvent`, `DocumentAddedEvent`, `CaseUpdatedEvent` — perzistirani u `case_timeline_event`

**Audit:**
- `AuditRecord` — perzistiran u `audit_record`

### 4.2 Perzistencija (JPA tabele)

| Tabela | Sadržaj |
|--------|---------|
| `legal_case` | Predmet + `last_status`, `last_evaluated_at`, `last_open_tasks` |
| `party`, `document`, `date_fact` | Podaci predmeta |
| `case_indicator` | L2 indikatori + `claim_amount` |
| `case_timeline_event` | CEP timeline |
| `audit_record` | Audit log |
| `system_state` | Simulacioni sat |

### 4.3 Checklist dokumentacije (L3 konfiguracija)

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

Dokumenti u enum-u van checkliste: `ID_CARD`, `REGISTRATION_EXTRACT`, `OTHER` — unos u formi, bez L3 pravila.

---

## 5. Struktura pravila i nivoi rezonovanja

**Ukupno:** 43 forward pravila (32 statička + 11 generisanih) + 8 BC upita.

**Salience (veći = ranije):** L2 initial (100) → L2 (50) → DATES (45) → L3 (40) → L4 doc (35) → L5 (30) → Procedure blocked (20) → CEP (15) → Procedure reachable (10) → Unknown / Select status (1).

### 5.1 L1 — Validacija osnovnih podataka

**Fajl:** `validation_rules.drl`

| Pravilo | Uslov | Ishod |
|---|---|---|
| Missing case name | `name` prazan | `MissingRequiredData(CASE_NAME)` |
| Missing description | `description` prazan | `MissingRequiredData(CASE_DESCRIPTION)` |
| Missing opposing party | nema `Party(OPPOSING)` | `MissingRequiredData(OPPOSING_PARTY)` |
| Missing contact | `Party(CLIENT)` bez kontakta | `MissingRequiredData(CONTACT)` |
| Basic data complete | nema `MissingRequiredData` | `BasicDataStatus(COMPLETE)` |

### 5.2 L2 — Preliminarna klasifikacija

**Fajl:** `classification_rules.drl`

| Pravilo | Salience | Uslov | Ishod |
|---|---|---|---|
| Classify from initial case type | 100 | `initialCaseType` ≠ null/UNKNOWN | `CaseClassification(type, HIGH)` |
| Classify debt collection | 50 | `hasDebtOrClaim` + (`hasInvoice` ili `DUE_DATE` ili doc `INVOICE`) | `DEBT_COLLECTION`, HIGH |
| Classify damages | 50 | `hasDamage` + (`DAMAGE_PROOF` ili `DAMAGE_REPORT` prisutan) | `DAMAGES`, HIGH |
| Classify property | 50 | `hasRealEstate` ili `hasCadastreData` | `PROPERTY`, HIGH |
| Unknown case type | 1 | nema klasifikacije | `UNKNOWN`, LOW + `MANUAL_CASE_TYPE_REVIEW` |

### 5.3 L3 — Provera dokumentacije (template)

**Fajlovi:** `document_checklist_template.drt` + `document_checklist_data.xlsx`

Zajednička logika: klasifikovan predmet + (opciono) tip stranke + dokument nije prisutan → `MissingRequiredDocument` ili `MissingExpectedDocument` + `SuggestedTask`.

Sva 11 pravila odgovaraju tabeli u §4.3. Generisana imena: npr. `DEBT_COLLECTION_missing_CONTRACT_2`.

### 5.4 L4 — Accumulate

**Fajl:** `accumulate_rules.drl`  
**Globali:** `officeWaitingThreshold=2`, `officeReadyThreshold=2`

| Pravilo | Uslov | Ishod |
|---|---|---|
| Documentation complete | 0 `MissingRequiredDocument` | `COMPLETE` |
| Documentation partial | 1–2 nedostajuća | `PARTIAL` |
| Documentation incomplete | >2 nedostajuća | `INCOMPLETE` |
| Many cases waiting for client | ≥ prag predmeta u `WAITING_FOR_CLIENT` | `OfficeLoadWarning(MANY_CASES_WAITING_FOR_DOCUMENTS)` |
| Many ready cases | ≥ prag predmeta u `READY_FOR_REVIEW` | `OfficeLoadWarning(MANY_CASES_READY_FOR_REVIEW)` |

### 5.5 L5 — Status predmeta

**Fajl:** `case_status_rules.drl`

| Prioritet | Status | Pravilo-kandidat |
|---|---|---|
| 1 | `INCOMPLETE` | `MissingRequiredData` |
| 2 | `WAITING_FOR_CLIENT` | `MissingRequiredDocument` |
| 3 | `NEEDS_ATTENTION` | `CepAlert` ili `CaseInactive` |
| 4 | `NEEDS_DATE_CHECK` | `ImportantDateNeedsCheck` |
| 5 | `READY_FOR_REVIEW` | L1 kompletan + L4 COMPLETE + nema required docs + nema otvorenih zadataka + nije arhiviran |
| 6 | `ARCHIVED` | `LegalCase.archived == true` |

**Select final case status:** bira kandidata sa **najmanjim** brojem prioriteta (jačim signalom).

### 5.6 CEP — Vremenski alarmi

**Fajl:** `cep_rules.drl` — pseudo-clock, događaji iz timeline-a.

| Pravilo | Uslov | Ishod |
|---|---|---|
| CEP missing document not added | `TaskCreatedEvent` + 7d bez `DocumentAddedEvent` + doc i dalje nedostaje | `DOCUMENT_NOT_ADDED` + `CHECK_DOCUMENT_REQUEST_STATUS` + kandidat p3 |
| CEP case waiting too long | `WAITING_FOR_CLIENT` + 14d bez update/doc događaja | `CASE_INACTIVE` + `CHECK_CASE_STATUS` |
| CEP ready case not reviewed | `READY_FOR_REVIEW` + 7d bez `CaseUpdatedEvent` | `READY_NOT_REVIEWED` + `ASSIGN_REVIEW` |
| Old case activity | 30d bez događaja | `CaseInactive(days)` + `CHECK_CASE_STATUS` |

Timeline se popunjava u `CaseTimelineService`: otvaranje/update predmeta, novi dokumenti, novi `REQUEST_*` zadaci.

### 5.7 Pravila za datume

**Fajl:** `date_rules.drl`

| Pravilo | Uslov | Ishod |
|---|---|---|
| Due date needs check | postoji `DUE_DATE` | `ImportantDateNeedsCheck` + `VERIFY_DUE_DATE` |
| Decision date needs check | postoji `DECISION_RECEIVED_DATE` | + `VERIFY_DATE_RELEVANCE` |
| Damage date needs check | postoji `DAMAGE_DATE` | + `VERIFY_DAMAGE_DATE` |

Sistem ne računa rokove — označava datume za ručnu proveru.

### 5.8 Procedure i backward chaining

**Forward:** `procedure_rules.drl` — za klasifikovani tip poziva `isStepBlocked(cid, goal)` → `MainActionAssessment`.

**Graf preduslova** (`ProcedurePrerequisiteRegistry`):

| Tip | Glavna radnja | Lanac (pojednostavljeno) |
|-----|---------------|--------------------------|
| DEBT_COLLECTION | `FILE_LAWSUIT` | tužba ← opomena ← slanje opomene ← osnov ← ugovor, faktura |
| DAMAGES | `FILE_DAMAGES_CLAIM` | tužba šteta ← osnov ← dokaz, zapisnik |
| PROPERTY | `FILE_PROPERTY_CLAIM` | tužba imovina ← vlasništvo ← katastar, vlasništvo doc |

**BC upiti** (`queries.drl`), API + UI panel:

| Upit | Namena |
|------|--------|
| `isCaseReadyForInitialReview` | Spreman za pregled? |
| `isBasicDataComplete`, `hasClassification`, … | Podciljevi |
| `isStepBlocked` | Rekurzivno — blokiran korak? |
| `dependsOnStep` | Tranzitivni preduslovi |
| `missingDocumentForGoal` | Svi blokirajući dokumenti |

Endpointi: `GET .../queries/ready-for-review`, `GET .../queries/processable`. UI: `BackwardChainingPanelComponent`.

### 5.9 Sažetak ulančavanja

1. L1 → 2. L2 → 3. L3 → 4. DATES → 5. L4 → 6. CEP → 7. L5 → 8. Procedure/BC.

---

## 6. Konkretan primer rezonovanja (ugrađeni Demo)

Korisnik klikne **Demo** u aplikaciji — backend kreira predmet naplate potraživanja.

**Ulaz:** potraživanje + faktura + ugovor; nedostaje `SERVICE_PROOF`; nedostaje opomena; `DUE_DATE`; pravno lice bez podataka o firmi.

1. L1 → `BasicDataStatus(COMPLETE)`.
2. L2 → `CaseClassification(DEBT_COLLECTION, HIGH)`.
3. L3 → `MissingRequiredDocument(SERVICE_PROOF)`, očekivani docs + zadaci (`REQUEST_SERVICE_PROOF`, `CHECK_IF_NOTICE_EXISTS`, `CHECK_COMPANY_DATA`).
4. L4 → `DocumentationChecklistStatus(PARTIAL)`.
5. DATES → `ImportantDateNeedsCheck(DUE_DATE)` + `VERIFY_DUE_DATE`.
6. L5 → kandidati p2 (čeka dopunu) i p4 (datumi); finalno **`WAITING_FOR_CLIENT`**.
7. Procedure → glavna radnja **Podnošenje tužbe — blokirana** (nema opomene).
8. UI prikazuje overview, zadatke, datume za proveru.

**Posle +7 dana simulacije + Ponovi procenu:**
9. CEP → `CepAlert(DOCUMENT_NOT_ADDED)` → status **`NEEDS_ATTENTION`**.

**BC upit** na Demo predmetu → `isCaseReadyForInitialReview` = **false** (nedostaje obavezni doc, otvoreni zadaci).

---

## 7. Primeri kompleksnih pravila

Implementirani su kao u README §7 — template L3, accumulate L4, CEP 14d prozor, prioritet statusa (L1 jači od datuma), BC upiti i rekurzivni `isStepBlocked`. Detaljni scenariji: [demo.md](demo.md).

---

## 8. Tehnička arhitektura (implementacija)

### 8.1 Pregled komponenti

| Komponenta | Tehnologija | Verzija / napomena |
|---|---|---|
| Rule Engine | Drools | 10.1.0 |
| Backend | Spring Boot | 4.0.5, Java 21 |
| Frontend | Angular (standalone) | 21 |
| Baza | PostgreSQL | 16 (Docker) |
| CEP | Drools STREAM + pseudo-clock | `cep_rules.drl` |
| Templejti | Drools Rule Templates + xlsx | L3 |
| Audit | JPA + `AuditService` | |

### 8.2 Backend

**Paket:** `com.sbnz.legal` — kontroleri, servisi, domen, perzistencija, config.

**REST API** (`/api/v1`):

| Grupa | Endpointi |
|-------|-----------|
| Predmeti | `GET/POST /cases`, `GET/PUT/DELETE /cases/{id}`, `GET .../edit`, `POST .../evaluate`, `POST /cases/demo`, `GET .../audit`, `GET .../queries/*` |
| Simulacija | `GET /simulation/clock`, `POST .../advance`, `POST .../reset` |

**Ključni servisi:** `CaseService`, `CaseRulesEngine`, `BackwardChainingService`, `CaseTimelineService`, `AuditService`, `SimulatedClockService`.

**Konfiguracija:** `application.properties` — DB, port 8080, pragovi office load.

### 8.3 Frontend

**Struktura:** `front-end/src/app/`

| Komponenta | Uloga |
|------------|-------|
| `App` | Layout, sidebar, orchestracija |
| `CaseFormComponent` | Unos / izmena predmeta |
| `CaseResultsComponent` | Prikaz `CaseReport` + audit |
| `BackwardChainingPanelComponent` | BC upiti |
| `SimulationDashboardComponent` | CEP simulacija (+1/+7/+14/+30, Reset) |
| `RuleCatalogComponent` | Statički katalog pravila (modal) |

**Servisi:** `CaseService`, `SimulationService` — mapiranje 1:1 na REST.

**Proxy:** `localhost:4200` → `localhost:8080/api`.

### 8.4 Pokretanje

```bash
docker compose up -d
cd back-end && ./mvnw spring-boot:run
cd front-end && npm install && npm start
```

Aplikacija: [http://localhost:4200](http://localhost:4200)

---

## 9. Ograničenja sistema

### 9.1 Domenska ograničenja (kao u README)

Sistem ne daje pravne savete, ne računa rokove i ne zamenjuje pravnika.

### 9.2 Ograničenja implementacije

| Oblast | Stanje |
|--------|--------|
| `claimAmount` | Čuva se u bazi; nema pravila ni prikaza u rezultatima |
| Datumi `OBLIGATION_DATE`, `LAST_ACTION_DATE`, `OPENED_DATE` | Samo perzistencija |
| Dokumenti van L3 checkliste | Enum + forma; bez pravila |
| Tip protivnika u UI | Uvek `LEGAL_ENTITY` |
| Zatvaranje zadataka | Nema API — zadaci nestaju pri re-evaluaciji kad uslov padne |
| `READY_FOR_REVIEW` | Zahteva sve obavezne **i** očekivane docs + bez datuma (VERIFY zadaci) |
| Katalog pravila u UI | Statički JSON; nije live vezan za `.drl` |
| Rule firings | Imena generisanih L3 pravila su tehnička (npr. `DEBT_COLLECTION_missing_CONTRACT_2`) |

---

## 10. Reference

[1] Clio — Legal Case Management Software  
[2] MyCase — Legal Case Management and Legal Calendaring Software

**Projekat:** [README.md](README.md) — originalni predlog  
**Demonstracija:** [demo.md](demo.md) — scenariji za odbranu
