import {
  AuditRecordType,
  CaseType,
  CepAlertType,
  DateType,
  DocumentType,
  MissingDataField,
  PartyType,
  ProcedureStep,
  ProcessingStatus,
  TaskType,
} from '../models/case.types';

export const STATUS_LABELS: Record<ProcessingStatus, string> = {
  INCOMPLETE: 'Nepotpun',
  WAITING_FOR_CLIENT: 'Čeka dopunu od stranke',
  NEEDS_ATTENTION: 'Zahteva pažnju',
  NEEDS_DATE_CHECK: 'Proveriti datume',
  READY_FOR_REVIEW: 'Spreman za pregled',
  ARCHIVED: 'Arhiviran',
};

export const CASE_TYPE_LABELS: Record<CaseType, string> = {
  DEBT_COLLECTION: 'Naplata potraživanja',
  DAMAGES: 'Naknada štete',
  PROPERTY: 'Imovinsko-pravni predmet',
  UNKNOWN: 'Nepoznat tip',
};

export const PARTY_TYPE_LABELS: Record<PartyType, string> = {
  NATURAL_PERSON: 'Fizičko lice',
  LEGAL_ENTITY: 'Pravno lice',
  PUBLIC_ENTITY: 'Javni organ',
  ANY: 'Bilo koji tip',
};

export const DOCUMENT_LABELS: Record<DocumentType, string> = {
  CONTRACT: 'Ugovor',
  INVOICE: 'Faktura',
  SERVICE_PROOF: 'Dokaz o izvršenoj usluzi',
  PRE_LAWSUIT_NOTICE: 'Opomena pre tužbe',
  DAMAGE_PROOF: 'Dokaz o šteti',
  DAMAGE_REPORT: 'Zapisnik o šteti',
  CADASTRE_EXTRACT: 'Izvod iz katastra',
  OWNERSHIP_DOCUMENT: 'Dokaz o vlasništvu',
  ID_CARD: 'Lična karta',
  REGISTRATION_EXTRACT: 'Izvod iz registra',
  COMPANY_REGISTRATION_DATA: 'Podaci o registraciji firme',
  PERSONAL_DATA: 'Lični podaci',
  AUTHORIZATION_OR_DECISION: 'Ovlašćenje ili odluka organa',
  OTHER: 'Ostalo',
};

export const DATE_LABELS: Record<DateType, string> = {
  DUE_DATE: 'Datum dospelosti',
  DAMAGE_DATE: 'Datum nastanka štete',
  DECISION_RECEIVED_DATE: 'Datum prijema odluke/akta',
  OBLIGATION_DATE: 'Datum nastanka obaveze',
  LAST_ACTION_DATE: 'Datum poslednje radnje',
  OPENED_DATE: 'Datum otvaranja predmeta',
};

export const TASK_LABELS: Record<TaskType, string> = {
  MANUAL_CASE_TYPE_REVIEW: 'Ručna provera tipa predmeta',
  REQUEST_CONTRACT: 'Zatražiti ugovor',
  REQUEST_INVOICE: 'Zatražiti fakturu',
  REQUEST_SERVICE_PROOF: 'Zatražiti dokaz o usluzi',
  CHECK_IF_NOTICE_EXISTS: 'Proveriti da li postoji opomena',
  CHECK_IF_DAMAGE_REPORT_EXISTS: 'Proveriti zapisnik o šteti',
  REQUEST_DAMAGE_PROOF: 'Zatražiti dokaz o šteti',
  REQUEST_CADASTRE_EXTRACT: 'Zatražiti katastarski izvod',
  REQUEST_OWNERSHIP_DOCUMENT: 'Zatražiti dokaz o vlasništvu',
  CHECK_AUTHORIZATION_DOCUMENT: 'Proveriti ovlašćenje organa',
  VERIFY_DUE_DATE: 'Proveriti datum dospelosti',
  VERIFY_DAMAGE_DATE: 'Proveriti datum štete',
  VERIFY_DATE_RELEVANCE: 'Proveriti relevantnost datuma',
  ASSIGN_REVIEW: 'Dodeliti pregled pravniku',
  CHECK_CASE_STATUS: 'Proveriti status predmeta',
  CHECK_DOCUMENT_REQUEST_STATUS: 'Proveriti status zahteva za dokument',
  CHECK_COMPANY_DATA: 'Proveriti podatke o firmi',
  CHECK_PERSONAL_DATA: 'Proveriti lične podatke',
};

export const CEP_ALERT_LABELS: Record<CepAlertType, string> = {
  DOCUMENT_NOT_ADDED: 'Dokument nije dodat u roku',
  CASE_INACTIVE: 'Predmet predugo čeka klijenta',
  READY_NOT_REVIEWED: 'Spreman predmet nije pregledan',
};

export const MISSING_FIELD_LABELS: Record<MissingDataField, string> = {
  CASE_NAME: 'Naziv predmeta',
  OPPOSING_PARTY: 'Druga strana',
  CASE_DESCRIPTION: 'Opis situacije',
  CONTACT: 'Kontakt stranke',
};

export const AUDIT_LABELS: Record<AuditRecordType, string> = {
  RULE_FIRED: 'Pravilo aktivirano',
  STATUS_CHANGED: 'Promena statusa',
  TASK_CREATED: 'Zadatak kreiran',
  CASE_UPDATED: 'Predmet ažuriran',
  CASE_DELETED: 'Predmet obrisan',
};

export const OFFICE_LOAD_LABELS: Record<string, string> = {
  MANY_CASES_WAITING_FOR_DOCUMENTS: 'Mnogo predmeta čeka dokumentaciju',
  MANY_CASES_READY_FOR_REVIEW: 'Mnogo predmeta spremno za pregled',
};

export const DOC_STATUS_LABELS: Record<string, string> = {
  COMPLETE: 'Kompletna',
  PARTIAL: 'Delimična',
  INCOMPLETE: 'Nekompletna',
};

export const PROCEDURE_STEP_LABELS: Record<ProcedureStep, string> = {
  ESTABLISH_CLAIM_BASIS: 'Utvrđivanje osnova potraživanja',
  SEND_PRELAWSUIT_NOTICE: 'Slanje opomene pre tužbe',
  FILE_LAWSUIT: 'Podnošenje tužbe',
  ESTABLISH_DAMAGE_BASIS: 'Utvrđivanje osnova štete',
  FILE_DAMAGES_CLAIM: 'Podnošenje zahteva za naknadu štete',
  ESTABLISH_OWNERSHIP: 'Utvrđivanje vlasništva',
  FILE_PROPERTY_CLAIM: 'Podnošenje imovinsko-pravnog zahteva',
};

export function labelStatus(value?: ProcessingStatus | string | null): string {
  if (!value) return '—';
  return STATUS_LABELS[value as ProcessingStatus] ?? value;
}

export function labelCaseType(value?: CaseType | string | null): string {
  if (!value) return '—';
  return CASE_TYPE_LABELS[value as CaseType] ?? value;
}

export function labelPartyType(value?: PartyType | string | null): string {
  if (!value) return '—';
  return PARTY_TYPE_LABELS[value as PartyType] ?? value;
}

export function labelDocument(value?: DocumentType | string | null): string {
  if (!value) return '—';
  return DOCUMENT_LABELS[value as DocumentType] ?? value;
}

export function labelDate(value?: DateType | string | null): string {
  if (!value) return '—';
  return DATE_LABELS[value as DateType] ?? value;
}

export function labelTask(value?: TaskType | string | null): string {
  if (!value) return '—';
  return TASK_LABELS[value as TaskType] ?? value;
}

export function labelCepAlert(value?: CepAlertType | string | null): string {
  if (!value) return '—';
  return CEP_ALERT_LABELS[value as CepAlertType] ?? value;
}

export function labelMissingField(value?: MissingDataField | string | null): string {
  if (!value) return '—';
  return MISSING_FIELD_LABELS[value as MissingDataField] ?? value;
}

export function labelAudit(value?: AuditRecordType | string | null): string {
  if (!value) return '—';
  return AUDIT_LABELS[value as AuditRecordType] ?? value;
}

export function labelOfficeLoad(value?: string | null): string {
  if (!value) return '—';
  return OFFICE_LOAD_LABELS[value] ?? value;
}

export function labelDocStatus(value?: string | null): string {
  if (!value) return '—';
  return DOC_STATUS_LABELS[value] ?? value;
}

export function labelProcedureStep(value?: ProcedureStep | string | null): string {
  if (!value) return '—';
  return PROCEDURE_STEP_LABELS[value as ProcedureStep] ?? value;
}
