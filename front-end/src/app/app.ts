import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { BackwardChainingPanelComponent } from './components/backward-chaining-panel/backward-chaining-panel.component';
import { CaseFormComponent } from './components/case-form/case-form.component';
import { CaseResultsComponent } from './components/case-results/case-results.component';
import { RuleCatalogComponent } from './components/rule-catalog/rule-catalog.component';
import { SimulationDashboardComponent } from './components/simulation-dashboard/simulation-dashboard.component';
import { AuditRecord, CaseReport, CreateCaseRequest, UpdateCaseRequest } from './models/case.types';
import { CaseService } from './services/case.service';
import { SimulationService } from './services/simulation.service';
import { labelStatus } from './utils/case-labels';

@Component({
  selector: 'app-root',
  imports: [
    BackwardChainingPanelComponent,
    CaseFormComponent,
    CaseResultsComponent,
    RuleCatalogComponent,
    SimulationDashboardComponent,
    DatePipe,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly cases = inject(CaseService);
  private readonly simulation = inject(SimulationService);

  protected readonly title = signal('SBNZ — Pravni predmeti');
  protected readonly caseList = signal<CaseReport[]>([]);
  protected readonly report = signal<CaseReport | null>(null);
  protected readonly auditLog = signal<AuditRecord[]>([]);
  protected readonly simulatedNow = signal<string | null>(null);
  protected readonly editCaseId = signal<string | null>(null);
  protected readonly editFormData = signal<UpdateCaseRequest | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly labelStatus = labelStatus;

  ngOnInit() {
    this.refreshCaseList();
    this.simulation.clock().subscribe({
      next: (s) => this.simulatedNow.set(s.simulatedNow),
      error: () => this.simulatedNow.set(null),
    });
  }

  protected advanceSimulation(days: number) {
    this.busy.set(true);
    this.error.set(null);
    this.simulation.advance(days).subscribe({
      next: (s) => this.afterSimulationUpdate(s.simulatedNow),
      error: () => {
        this.error.set('Simulacija vremena nije uspela.');
        this.busy.set(false);
      },
    });
  }

  protected resetSimulation() {
    this.busy.set(true);
    this.error.set(null);
    this.simulation.reset().subscribe({
      next: (s) => this.afterSimulationUpdate(s.simulatedNow),
      error: () => {
        this.error.set('Reset simulacionog sata nije uspeo.');
        this.busy.set(false);
      },
    });
  }

  protected loadDemo() {
    this.clearEditMode();
    this.busy.set(true);
    this.error.set(null);
    this.cases.demo().subscribe({
      next: (r) => this.applyReport(r),
      error: () => {
        this.error.set('Backend nije dostupan. Pokreni Spring Boot i PostgreSQL (docker compose up).');
        this.busy.set(false);
      },
    });
  }

  protected refresh() {
    const id = this.report()?.caseId;
    if (!id) return;
    this.busy.set(true);
    this.cases.evaluate(id).subscribe({
      next: (r) => this.applyReport(r),
      error: () => {
        this.error.set('Procena nije uspela.');
        this.busy.set(false);
      },
    });
  }

  protected selectCase(caseId: string) {
    this.busy.set(true);
    this.error.set(null);
    this.editCaseId.set(caseId);
    this.cases.get(caseId).subscribe({
      next: (r) => {
        this.applyReport(r);
        this.cases.getForEdit(caseId).subscribe({
          next: (data) => this.editFormData.set(data),
          error: () => this.editFormData.set(null),
        });
      },
      error: () => {
        this.error.set('Učitavanje predmeta nije uspelo.');
        this.busy.set(false);
      },
    });
  }

  protected startNewCase() {
    this.clearEditMode();
    this.report.set(null);
    this.auditLog.set([]);
  }

  protected onCreate(req: CreateCaseRequest) {
    this.busy.set(true);
    this.error.set(null);
    this.cases.create(req).subscribe({
      next: (r) => this.applyReport(r),
      error: () => {
        this.error.set('Kreiranje predmeta nije uspelo.');
        this.busy.set(false);
      },
    });
  }

  protected onUpdate(event: { caseId: string; request: UpdateCaseRequest }) {
    this.busy.set(true);
    this.error.set(null);
    this.cases.update(event.caseId, event.request).subscribe({
      next: (r) => this.applyReport(r),
      error: () => {
        this.error.set('Ažuriranje predmeta nije uspelo.');
        this.busy.set(false);
      },
    });
  }

  protected deleteCurrentCase() {
    const id = this.report()?.caseId;
    if (!id || !confirm('Obrisati predmet?')) return;
    this.busy.set(true);
    this.error.set(null);
    this.cases.delete(id).subscribe({
      next: () => {
        this.startNewCase();
        this.refreshCaseList();
        this.busy.set(false);
      },
      error: () => {
        this.error.set('Brisanje predmeta nije uspelo.');
        this.busy.set(false);
      },
    });
  }

  protected onCancelEdit() {
    const id = this.report()?.caseId;
    if (!id) {
      this.clearEditMode();
      return;
    }
    this.cases.getForEdit(id).subscribe({
      next: (data) => this.editFormData.set(data),
    });
  }

  private applyReport(report: CaseReport) {
    this.report.set(report);
    this.editCaseId.set(report.caseId);
    if (report.simulatedNow) {
      this.simulatedNow.set(report.simulatedNow);
    }
    this.cases.audit(report.caseId).subscribe({
      next: (entries) => {
        this.auditLog.set(entries);
        this.refreshCaseList();
        this.cases.getForEdit(report.caseId).subscribe({
          next: (data) => this.editFormData.set(data),
        });
        this.busy.set(false);
      },
      error: () => {
        this.auditLog.set([]);
        this.refreshCaseList();
        this.busy.set(false);
      },
    });
  }

  private refreshCaseList() {
    this.cases.list().subscribe({
      next: (items) => this.caseList.set(items),
      error: () => this.caseList.set([]),
    });
  }

  private clearEditMode() {
    this.editCaseId.set(null);
    this.editFormData.set(null);
  }

  private afterSimulationUpdate(simulatedNow: string) {
    this.simulatedNow.set(simulatedNow);
    const id = this.report()?.caseId;
    if (id) {
      this.cases.get(id).subscribe({
        next: (r) => this.applyReport(r),
        error: () => this.busy.set(false),
      });
    } else {
      this.refreshCaseList();
      this.busy.set(false);
    }
  }
}
