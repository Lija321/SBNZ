import { Component, input } from '@angular/core';
import { AuditRecord, CaseReport, ProcessingStatus } from '../../models/case.types';
import {
  labelAudit,
  labelCaseType,
  labelCepAlert,
  labelDate,
  labelDocStatus,
  labelDocument,
  labelMissingField,
  labelOfficeLoad,
  labelProcedureStep,
  labelStatus,
  labelTask,
} from '../../utils/case-labels';

@Component({
  selector: 'case-results',
  standalone: true,
  templateUrl: './case-results.component.html',
  styleUrl: './case-results.component.scss',
})
export class CaseResultsComponent {
  readonly report = input<CaseReport | null>(null);
  readonly auditLog = input<AuditRecord[]>([]);

  protected readonly labelStatus = labelStatus;
  protected readonly labelCaseType = labelCaseType;
  protected readonly labelDocStatus = labelDocStatus;
  protected readonly labelDate = labelDate;
  protected readonly labelCepAlert = labelCepAlert;
  protected readonly labelMissingField = labelMissingField;
  protected readonly labelDocument = labelDocument;
  protected readonly labelTask = labelTask;
  protected readonly labelOfficeLoad = labelOfficeLoad;
  protected readonly labelProcedureStep = labelProcedureStep;
  protected readonly labelAudit = labelAudit;

  protected statusTone(status?: ProcessingStatus): 'warn' | 'ok' | 'neutral' {
    switch (status) {
      case 'NEEDS_ATTENTION':
      case 'INCOMPLETE':
      case 'WAITING_FOR_CLIENT':
        return 'warn';
      case 'READY_FOR_REVIEW':
        return 'ok';
      default:
        return 'neutral';
    }
  }

  protected openTasks(r: CaseReport) {
    return (r.suggestedTasks ?? []).filter((t) => t.open);
  }

  protected hasAttention(r: CaseReport): boolean {
    return !!(
      r.cepAlerts?.length ||
      r.caseInactive ||
      r.officeLoadWarnings?.length ||
      r.missingRequiredDocuments?.length
    );
  }

  protected hasActions(r: CaseReport): boolean {
    return !!(
      this.openTasks(r).length ||
      r.importantDatesToCheck?.length ||
      r.missingFields?.length ||
      r.missingExpectedDocuments?.length
    );
  }

  protected showDemoHint(r: CaseReport): boolean {
    return r.summary?.status === 'WAITING_FOR_CLIENT' && !(r.cepAlerts?.length);
  }
}
