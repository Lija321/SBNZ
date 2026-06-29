import { Component, effect, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CaseType,
  CreateCaseRequest,
  DateFact,
  DateType,
  Document,
  DocumentType,
  PartyType,
  UpdateCaseRequest,
} from '../../models/case.types';
import { CASE_TYPE_LABELS, DOCUMENT_LABELS, PARTY_TYPE_LABELS } from '../../utils/case-labels';

interface DocumentField {
  type: DocumentType;
  label: string;
}

@Component({
  selector: 'case-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './case-form.component.html',
  styleUrl: './case-form.component.scss',
})
export class CaseFormComponent {
  readonly editCaseId = input<string | null>(null);
  readonly initialData = input<UpdateCaseRequest | null>(null);

  readonly submitCreate = output<CreateCaseRequest>();
  readonly submitUpdate = output<{ caseId: string; request: UpdateCaseRequest }>();
  readonly cancelEdit = output<void>();

  protected readonly partyTypeOptions = Object.entries(PARTY_TYPE_LABELS)
    .filter(([value]) => value !== 'ANY')
    .map(([value, label]) => ({ value: value as PartyType, label }));

  protected readonly caseTypeOptions = [
    { value: '', label: 'Automatska klasifikacija (L2)' },
    ...Object.entries(CASE_TYPE_LABELS).map(([value, label]) => ({
      value: value as CaseType,
      label,
    })),
  ];

  protected readonly debtDocuments: DocumentField[] = [
    { type: 'CONTRACT', label: DOCUMENT_LABELS.CONTRACT },
    { type: 'INVOICE', label: DOCUMENT_LABELS.INVOICE },
    { type: 'SERVICE_PROOF', label: DOCUMENT_LABELS.SERVICE_PROOF },
    { type: 'PRE_LAWSUIT_NOTICE', label: DOCUMENT_LABELS.PRE_LAWSUIT_NOTICE },
  ];

  protected readonly damageDocuments: DocumentField[] = [
    { type: 'DAMAGE_PROOF', label: DOCUMENT_LABELS.DAMAGE_PROOF },
    { type: 'DAMAGE_REPORT', label: DOCUMENT_LABELS.DAMAGE_REPORT },
  ];

  protected readonly propertyDocuments: DocumentField[] = [
    { type: 'CADASTRE_EXTRACT', label: DOCUMENT_LABELS.CADASTRE_EXTRACT },
    { type: 'OWNERSHIP_DOCUMENT', label: DOCUMENT_LABELS.OWNERSHIP_DOCUMENT },
    { type: 'AUTHORIZATION_OR_DECISION', label: DOCUMENT_LABELS.AUTHORIZATION_OR_DECISION },
  ];

  protected readonly identityDocuments: DocumentField[] = [
    { type: 'ID_CARD', label: DOCUMENT_LABELS.ID_CARD },
    { type: 'REGISTRATION_EXTRACT', label: DOCUMENT_LABELS.REGISTRATION_EXTRACT },
    { type: 'PERSONAL_DATA', label: DOCUMENT_LABELS.PERSONAL_DATA },
    { type: 'COMPANY_REGISTRATION_DATA', label: DOCUMENT_LABELS.COMPANY_REGISTRATION_DATA },
  ];

  protected readonly otherDocuments: DocumentField[] = [
    { type: 'OTHER', label: DOCUMENT_LABELS.OTHER },
  ];

  protected readonly allDocumentTypes: DocumentType[] = [
    'CONTRACT',
    'INVOICE',
    'SERVICE_PROOF',
    'PRE_LAWSUIT_NOTICE',
    'DAMAGE_PROOF',
    'DAMAGE_REPORT',
    'CADASTRE_EXTRACT',
    'OWNERSHIP_DOCUMENT',
    'AUTHORIZATION_OR_DECISION',
    'ID_CARD',
    'REGISTRATION_EXTRACT',
    'PERSONAL_DATA',
    'COMPANY_REGISTRATION_DATA',
    'OTHER',
  ];

  name = '';
  description = '';
  assignedUser = 'kancelarija';
  initialCaseType = '';
  clientPartyType: PartyType = 'LEGAL_ENTITY';
  clientName = '';
  clientContact = '';
  opposingName = '';
  hasDebtOrClaim = false;
  hasInvoice = false;
  hasContract = false;
  hasDamage = false;
  hasRealEstate = false;
  hasCadastreData = false;
  hasOfficialAct = false;
  claimAmount: number | null = null;
  dueDate = '';
  damageDate = '';
  decisionReceivedDate = '';
  obligationDate = '';
  lastActionDate = '';
  openedDate = '';
  documentPresent: Record<DocumentType, boolean> = this.emptyDocuments();
  archived = false;

  constructor() {
    effect(() => {
      const data = this.initialData();
      const editId = this.editCaseId();
      if (data) {
        this.loadFromRequest(data);
      } else if (!editId) {
        this.resetForm();
      }
    });
  }

  resetForm() {
    this.name = '';
    this.description = '';
    this.assignedUser = 'kancelarija';
    this.initialCaseType = '';
    this.clientPartyType = 'LEGAL_ENTITY';
    this.clientName = '';
    this.clientContact = '';
    this.opposingName = '';
    this.hasDebtOrClaim = false;
    this.hasInvoice = false;
    this.hasContract = false;
    this.hasDamage = false;
    this.hasRealEstate = false;
    this.hasCadastreData = false;
    this.hasOfficialAct = false;
    this.claimAmount = null;
    this.dueDate = '';
    this.damageDate = '';
    this.decisionReceivedDate = '';
    this.obligationDate = '';
    this.lastActionDate = '';
    this.openedDate = '';
    this.documentPresent = this.emptyDocuments();
    this.archived = false;
  }

  onSubmit() {
    const request = this.buildRequest();
    const caseId = this.editCaseId();
    if (caseId) {
      this.submitUpdate.emit({ caseId, request });
    } else {
      this.submitCreate.emit(request);
    }
  }

  onCancel() {
    this.cancelEdit.emit();
  }

  private loadFromRequest(req: UpdateCaseRequest) {
    this.name = req.name;
    this.description = req.description;
    this.assignedUser = req.assignedUser;
    this.initialCaseType = req.initialCaseType ?? '';
    this.archived = req.archived ?? false;

    const client = req.parties.find((p) => p.role === 'CLIENT');
    const opposing = req.parties.find((p) => p.role === 'OPPOSING');
    this.clientPartyType = client?.partyType ?? 'LEGAL_ENTITY';
    this.clientName = client?.name ?? '';
    this.clientContact = client?.contact ?? '';
    this.opposingName = opposing?.name ?? '';

    this.documentPresent = this.emptyDocuments();
    for (const doc of req.documents) {
      this.documentPresent[doc.documentType] = doc.present;
    }

    this.dueDate = this.dateValue(req, 'DUE_DATE');
    this.damageDate = this.dateValue(req, 'DAMAGE_DATE');
    this.decisionReceivedDate = this.dateValue(req, 'DECISION_RECEIVED_DATE');
    this.obligationDate = this.dateValue(req, 'OBLIGATION_DATE');
    this.lastActionDate = this.dateValue(req, 'LAST_ACTION_DATE');
    this.openedDate = this.dateValue(req, 'OPENED_DATE');

    this.hasDebtOrClaim = req.indicators.hasDebtOrClaim;
    this.hasDamage = req.indicators.hasDamage;
    this.hasContract = req.indicators.hasContract;
    this.hasInvoice = req.indicators.hasInvoice;
    this.hasRealEstate = req.indicators.hasRealEstate;
    this.hasCadastreData = req.indicators.hasCadastreData;
    this.hasOfficialAct = req.indicators.hasOfficialAct;
    this.claimAmount = req.indicators.claimAmount ?? null;
  }

  private buildRequest(): UpdateCaseRequest {
    const documents: Document[] = this.allDocumentTypes.map((documentType) => ({
      documentType,
      present: this.documentPresent[documentType] ?? false,
    }));

    const dateFacts: DateFact[] = [];
    this.appendDateFact(dateFacts, 'DUE_DATE', this.dueDate);
    this.appendDateFact(dateFacts, 'DAMAGE_DATE', this.damageDate);
    this.appendDateFact(dateFacts, 'DECISION_RECEIVED_DATE', this.decisionReceivedDate);
    this.appendDateFact(dateFacts, 'OBLIGATION_DATE', this.obligationDate);
    this.appendDateFact(dateFacts, 'LAST_ACTION_DATE', this.lastActionDate);
    this.appendDateFact(dateFacts, 'OPENED_DATE', this.openedDate);

    return {
      name: this.name,
      description: this.description,
      assignedUser: this.assignedUser,
      initialCaseType: this.initialCaseType ? (this.initialCaseType as CaseType) : undefined,
      archived: this.archived,
      parties: [
        {
          role: 'CLIENT',
          partyType: this.clientPartyType,
          name: this.clientName,
          contact: this.clientContact,
        },
        {
          role: 'OPPOSING',
          partyType: 'LEGAL_ENTITY',
          name: this.opposingName,
        },
      ],
      documents,
      dateFacts: dateFacts.length ? dateFacts : undefined,
      indicators: {
        hasDebtOrClaim: this.hasDebtOrClaim,
        hasDamage: this.hasDamage,
        hasContract: this.hasContract,
        hasInvoice: this.hasInvoice,
        hasRealEstate: this.hasRealEstate,
        hasCadastreData: this.hasCadastreData,
        hasOfficialAct: this.hasOfficialAct,
        claimAmount: this.claimAmount,
      },
    };
  }

  private appendDateFact(dateFacts: DateFact[], dateType: DateType, value: string) {
    if (value) {
      dateFacts.push({ dateType, value });
    }
  }

  private emptyDocuments(): Record<DocumentType, boolean> {
    return {
      CONTRACT: false,
      INVOICE: false,
      SERVICE_PROOF: false,
      PRE_LAWSUIT_NOTICE: false,
      DAMAGE_PROOF: false,
      DAMAGE_REPORT: false,
      CADASTRE_EXTRACT: false,
      OWNERSHIP_DOCUMENT: false,
      ID_CARD: false,
      REGISTRATION_EXTRACT: false,
      COMPANY_REGISTRATION_DATA: false,
      PERSONAL_DATA: false,
      AUTHORIZATION_OR_DECISION: false,
      OTHER: false,
    };
  }

  private dateValue(req: UpdateCaseRequest, type: DateType): string {
    return req.dateFacts?.find((d) => d.dateType === type)?.value ?? '';
  }
}
