export type ProcessingStatus =
  | 'INCOMPLETE'
  | 'WAITING_FOR_CLIENT'
  | 'NEEDS_ATTENTION'
  | 'NEEDS_DATE_CHECK'
  | 'READY_FOR_REVIEW'
  | 'ARCHIVED';

export type CaseType = 'DEBT_COLLECTION' | 'DAMAGES' | 'PROPERTY' | 'UNKNOWN';

export type PartyRole = 'CLIENT' | 'OPPOSING';
export type PartyType = 'NATURAL_PERSON' | 'LEGAL_ENTITY' | 'PUBLIC_ENTITY' | 'ANY';

export type DocumentType =
  | 'CONTRACT'
  | 'INVOICE'
  | 'SERVICE_PROOF'
  | 'PRE_LAWSUIT_NOTICE'
  | 'DAMAGE_PROOF'
  | 'DAMAGE_REPORT'
  | 'CADASTRE_EXTRACT'
  | 'OWNERSHIP_DOCUMENT'
  | 'ID_CARD'
  | 'REGISTRATION_EXTRACT'
  | 'COMPANY_REGISTRATION_DATA'
  | 'PERSONAL_DATA'
  | 'AUTHORIZATION_OR_DECISION'
  | 'OTHER';

export type DateType =
  | 'DUE_DATE'
  | 'DAMAGE_DATE'
  | 'DECISION_RECEIVED_DATE'
  | 'OBLIGATION_DATE'
  | 'LAST_ACTION_DATE'
  | 'OPENED_DATE';

export type CepAlertType = 'DOCUMENT_NOT_ADDED' | 'CASE_INACTIVE' | 'READY_NOT_REVIEWED';

export interface CepAlert {
  caseId: string;
  alertType: CepAlertType;
  raisedAt: string;
}

export interface CaseInactive {
  caseId: string;
  daysSinceActivity: number;
}

export type TaskType =
  | 'MANUAL_CASE_TYPE_REVIEW'
  | 'REQUEST_CONTRACT'
  | 'REQUEST_INVOICE'
  | 'REQUEST_SERVICE_PROOF'
  | 'CHECK_IF_NOTICE_EXISTS'
  | 'CHECK_IF_DAMAGE_REPORT_EXISTS'
  | 'REQUEST_DAMAGE_PROOF'
  | 'REQUEST_CADASTRE_EXTRACT'
  | 'REQUEST_OWNERSHIP_DOCUMENT'
  | 'CHECK_AUTHORIZATION_DOCUMENT'
  | 'VERIFY_DUE_DATE'
  | 'VERIFY_DAMAGE_DATE'
  | 'VERIFY_DATE_RELEVANCE'
  | 'ASSIGN_REVIEW'
  | 'CHECK_CASE_STATUS'
  | 'CHECK_DOCUMENT_REQUEST_STATUS'
  | 'CHECK_COMPANY_DATA'
  | 'CHECK_PERSONAL_DATA';

export type DocumentationStatus = 'COMPLETE' | 'PARTIAL' | 'INCOMPLETE';

export type ClassificationConfidence = 'HIGH' | 'LOW';

export type MissingDataField = 'CASE_NAME' | 'OPPOSING_PARTY' | 'CASE_DESCRIPTION' | 'CONTACT';

export interface LegalCase {
  caseId: string;
  name: string;
  description: string;
  assignedUser: string;
  openedAt: string;
  lastUpdatedAt: string;
  archived: boolean;
  initialCaseType?: CaseType;
}

export interface Party {
  caseId?: string;
  role: PartyRole;
  partyType: PartyType;
  name: string;
  contact?: string;
}

export interface Document {
  caseId?: string;
  documentType: DocumentType;
  present: boolean;
}

export interface DateFact {
  caseId?: string;
  dateType: DateType;
  value: string;
}

export type ProcedureStep =
  | 'ESTABLISH_CLAIM_BASIS'
  | 'SEND_PRELAWSUIT_NOTICE'
  | 'FILE_LAWSUIT'
  | 'ESTABLISH_DAMAGE_BASIS'
  | 'FILE_DAMAGES_CLAIM'
  | 'ESTABLISH_OWNERSHIP'
  | 'FILE_PROPERTY_CLAIM';

export interface CaseClassification {
  caseId: string;
  caseType: CaseType;
  confidence: ClassificationConfidence;
}

export interface SuggestedTask {
  caseId: string;
  taskType: TaskType;
  open: boolean;
  createdAt: string;
}

export interface CaseSummaryView {
  status: ProcessingStatus;
  classification?: CaseType;
  documentation?: string;
}

export interface RuleFiring {
  ruleName: string;
  firedAt: string;
  explanation: string;
}

export type AuditRecordType =
  | 'RULE_FIRED'
  | 'STATUS_CHANGED'
  | 'TASK_CREATED'
  | 'CASE_UPDATED'
  | 'CASE_DELETED';

export interface AuditRecord {
  id: string;
  caseId: string;
  recordType: AuditRecordType;
  ruleName?: string;
  firedAt: string;
  explanation?: string;
  previousStatus?: ProcessingStatus;
  newStatus?: ProcessingStatus;
  taskType?: TaskType;
}

export interface CaseReport {
  caseId: string;
  legalCase: LegalCase;
  summary: CaseSummaryView;
  parties: Party[];
  documents: Document[];
  missingFields: MissingDataField[];
  missingRequiredDocuments: DocumentType[];
  missingExpectedDocuments: DocumentType[];
  documentationChecklistStatus?: {
    caseId: string;
    status: DocumentationStatus;
  };
  statusCandidates?: {
    caseId: string;
    status: ProcessingStatus;
    priority: number;
  }[];
  importantDatesToCheck?: DateType[];
  cepAlerts?: CepAlert[];
  caseInactive?: CaseInactive;
  officeLoadWarnings?: {
    type: string;
    caseCount: number;
  }[];
  suggestedTasks: SuggestedTask[];
  classification?: CaseClassification;
  mainActionGoal?: ProcedureStep;
  mainActionBlocked?: boolean;
  ruleFirings: RuleFiring[];
  evaluatedAt: string;
  simulatedNow?: string;
}

export interface BackwardChainingSubGoal {
  name: string;
  satisfied: boolean;
  detail: string;
}

export interface BackwardChainingReport {
  caseId: string;
  queryName: string;
  satisfied: boolean;
  summary: string;
  subGoals: BackwardChainingSubGoal[];
  blockers: string[];
}

export interface CreateCaseRequest {
  name: string;
  description: string;
  assignedUser: string;
  initialCaseType?: CaseType;
  parties: Party[];
  documents: Document[];
  dateFacts?: DateFact[];
  indicators: {
    hasDebtOrClaim: boolean;
    hasDamage: boolean;
    hasContract: boolean;
    hasInvoice: boolean;
    hasRealEstate: boolean;
    hasCadastreData: boolean;
    hasOfficialAct: boolean;
    claimAmount?: number | null;
  };
}

export interface UpdateCaseRequest extends CreateCaseRequest {
  archived?: boolean;
}
