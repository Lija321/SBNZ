import { Component, inject, input, signal } from '@angular/core';
import { BackwardChainingReport } from '../../models/case.types';
import { CaseService } from '../../services/case.service';

import { PROCEDURE_STEP_LABELS } from '../../utils/case-labels';

const SUBGOAL_LABELS: Record<string, string> = {
  // "ready for review" goal decomposition
  isBasicDataComplete: 'Osnovni podaci kompletni',
  hasClassification: 'Predmet klasifikovan',
  noMissingRequiredDocuments: 'Nema nedostajućih obaveznih dokumenata',
  noOpenBlockingTasks: 'Nema otvorenih blokirajućih zadataka',
  // procedural prerequisite steps (recursive goal)
  ...PROCEDURE_STEP_LABELS,
};

@Component({
  selector: 'backward-chaining-panel',
  standalone: true,
  templateUrl: './backward-chaining-panel.component.html',
  styleUrl: './backward-chaining-panel.component.scss',
})
export class BackwardChainingPanelComponent {
  private readonly cases = inject(CaseService);

  readonly caseId = input<string | null>(null);

  protected readonly report = signal<BackwardChainingReport | null>(null);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);

  protected runReadyForReview() {
    this.runQuery((id) => this.cases.readyForInitialReview(id));
  }

  protected runProcessable() {
    this.runQuery((id) => this.cases.caseProcessable(id));
  }

  protected subGoalLabel(name: string): string {
    return SUBGOAL_LABELS[name] ?? name;
  }

  private runQuery(call: (id: string) => ReturnType<CaseService['caseProcessable']>) {
    const id = this.caseId();
    if (!id) return;
    this.busy.set(true);
    this.error.set(null);
    call(id).subscribe({
      next: (r) => {
        this.report.set(r);
        this.busy.set(false);
      },
      error: () => {
        this.error.set('Izvršavanje upita nije uspelo.');
        this.busy.set(false);
      },
    });
  }
}
