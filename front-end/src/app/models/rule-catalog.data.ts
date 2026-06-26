import { RuleCatalogSection } from './rule-catalog.model';

export const RULE_CATALOG: RuleCatalogSection[] = [
  {
    id: 'L1',
    label: 'L1 — Validacija osnovnih podataka',
    rules: [
      { name: 'Missing case name', condition: 'Predmet nema naziv', outcome: 'MissingRequiredData(CASE_NAME)' },
      { name: 'Missing opposing party', condition: 'Nema druge strane', outcome: 'MissingRequiredData(OPPOSING_PARTY)' },
      { name: 'Missing description', condition: 'Nema opisa', outcome: 'MissingRequiredData(CASE_DESCRIPTION)' },
      { name: 'Missing contact', condition: 'Nema kontakta stranke', outcome: 'MissingRequiredData(CONTACT)' },
      { name: 'Basic data complete', condition: 'Nema MissingRequiredData', outcome: 'BasicDataStatus(COMPLETE)' },
    ],
  },
  {
    id: 'L2',
    label: 'L2 — Preliminarna klasifikacija',
    rules: [
      {
        name: 'Classify debt collection',
        condition: 'Potraživanje + faktura ili datum dospelosti',
        outcome: 'CaseClassification(DEBT_COLLECTION, HIGH)',
      },
      {
        name: 'Classify damages',
        condition: 'Šteta + dokaz ili zapisnik',
        outcome: 'CaseClassification(DAMAGES, HIGH)',
      },
      {
        name: 'Classify property',
        condition: 'Nepokretnost ili katastarski podaci',
        outcome: 'CaseClassification(PROPERTY, HIGH)',
      },
      {
        name: 'Unknown case type',
        condition: 'Nijedno L2 pravilo nije aktivirano',
        outcome: 'CaseClassification(UNKNOWN, LOW) + SuggestedTask(MANUAL_CASE_TYPE_REVIEW)',
      },
    ],
  },
  {
    id: 'L3',
    label: 'L3 — Checklist dokumentacije (template)',
    rules: [
      {
        name: 'Debt collection missing contract',
        condition: 'DEBT_COLLECTION + nema CONTRACT',
        outcome: 'MissingRequiredDocument + REQUEST_CONTRACT',
        fromTemplate: true,
      },
      {
        name: 'Debt collection missing service proof',
        condition: 'DEBT_COLLECTION + nema SERVICE_PROOF',
        outcome: 'MissingRequiredDocument + REQUEST_SERVICE_PROOF',
        fromTemplate: true,
      },
      {
        name: 'Debt collection missing notice',
        condition: 'DEBT_COLLECTION + nema PRE_LAWSUIT_NOTICE',
        outcome: 'MissingExpectedDocument + CHECK_IF_NOTICE_EXISTS',
        fromTemplate: true,
      },
      {
        name: 'Damages missing damage proof',
        condition: 'DAMAGES + nema DAMAGE_PROOF',
        outcome: 'MissingRequiredDocument + REQUEST_DAMAGE_PROOF',
        fromTemplate: true,
      },
      {
        name: 'Property missing cadastre extract',
        condition: 'PROPERTY + nema CADASTRE_EXTRACT',
        outcome: 'MissingRequiredDocument + REQUEST_CADASTRE_EXTRACT',
        fromTemplate: true,
      },
    ],
  },
  {
    id: 'L4',
    label: 'L4 — Accumulate',
    rules: [
      { name: 'Documentation complete', condition: '0 MissingRequiredDocument', outcome: 'DocumentationChecklistStatus(COMPLETE)' },
      { name: 'Documentation partial', condition: '1–2 nedostajuća', outcome: 'DocumentationChecklistStatus(PARTIAL)' },
      { name: 'Documentation incomplete', condition: '>2 nedostajuća', outcome: 'DocumentationChecklistStatus(INCOMPLETE)' },
      {
        name: 'Many cases waiting for client',
        condition: 'Broj WAITING_FOR_CLIENT ≥ prag',
        outcome: 'OfficeLoadWarning(MANY_CASES_WAITING_FOR_DOCUMENTS)',
      },
      {
        name: 'Many ready cases',
        condition: 'Broj READY_FOR_REVIEW ≥ prag',
        outcome: 'OfficeLoadWarning(MANY_CASES_READY_FOR_REVIEW)',
      },
    ],
  },
  {
    id: 'L5',
    label: 'L5 — Status predmeta',
    rules: [
      { name: 'Candidate incomplete', condition: 'MissingRequiredData', outcome: 'CaseStatusCandidate(INCOMPLETE, 1)' },
      { name: 'Candidate waiting for client', condition: 'MissingRequiredDocument', outcome: 'CaseStatusCandidate(WAITING_FOR_CLIENT, 2)' },
      { name: 'Candidate needs attention', condition: 'CepAlert ili CaseInactive', outcome: 'CaseStatusCandidate(NEEDS_ATTENTION, 3)' },
      { name: 'Candidate needs date check', condition: 'ImportantDateNeedsCheck', outcome: 'CaseStatusCandidate(NEEDS_DATE_CHECK, 4)' },
      {
        name: 'Candidate ready for review',
        condition: 'Podaci kompletni + dokumentacija kompletna + nema otvorenih zadataka',
        outcome: 'CaseStatusCandidate(READY_FOR_REVIEW, 5)',
      },
      { name: 'Select final case status', condition: 'Kandidat sa najmanjim prioritetom', outcome: 'CaseStatus' },
    ],
  },
  {
    id: 'CEP',
    label: 'CEP — Vremenski alarmi',
    rules: [
      {
        name: 'CEP missing document not added',
        condition: 'Zadatak za dokument + ≥7 dana bez dodavanja',
        outcome: 'CepAlert(DOCUMENT_NOT_ADDED) + CHECK_DOCUMENT_REQUEST_STATUS + NEEDS_ATTENTION',
      },
      {
        name: 'CEP case waiting too long',
        condition: 'WAITING_FOR_CLIENT + 14 dana bez aktivnosti',
        outcome: 'CepAlert(CASE_INACTIVE) + CHECK_CASE_STATUS',
      },
      {
        name: 'CEP ready case not reviewed',
        condition: 'READY_FOR_REVIEW + 7 dana bez ažuriranja',
        outcome: 'CepAlert(READY_NOT_REVIEWED) + ASSIGN_REVIEW',
      },
      {
        name: 'Old case activity',
        condition: 'Poslednja radnja starija od praga (30 dana)',
        outcome: 'CaseInactive + CHECK_CASE_STATUS',
      },
    ],
  },
  {
    id: 'DATES',
    label: 'Pravila za datume',
    rules: [
      { name: 'Due date needs check', condition: 'Postoji DUE_DATE', outcome: 'ImportantDateNeedsCheck + VERIFY_DUE_DATE' },
      {
        name: 'Decision date needs check',
        condition: 'Postoji DECISION_RECEIVED_DATE',
        outcome: 'ImportantDateNeedsCheck + VERIFY_DATE_RELEVANCE',
      },
      { name: 'Damage date needs check', condition: 'Postoji DAMAGE_DATE', outcome: 'ImportantDateNeedsCheck + VERIFY_DAMAGE_DATE' },
    ],
  },
];
