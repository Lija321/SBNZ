import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AuditRecord,
  BackwardChainingReport,
  CaseReport,
  CreateCaseRequest,
  UpdateCaseRequest,
} from '../models/case.types';

@Injectable({ providedIn: 'root' })
export class CaseService {
  private readonly base = '/api/v1/cases';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<CaseReport[]> {
    return this.http.get<CaseReport[]>(this.base);
  }

  get(caseId: string): Observable<CaseReport> {
    return this.http.get<CaseReport>(`${this.base}/${caseId}`);
  }

  getForEdit(caseId: string): Observable<UpdateCaseRequest> {
    return this.http.get<UpdateCaseRequest>(`${this.base}/${caseId}/edit`);
  }

  create(request: CreateCaseRequest): Observable<CaseReport> {
    return this.http.post<CaseReport>(this.base, request);
  }

  update(caseId: string, request: UpdateCaseRequest): Observable<CaseReport> {
    return this.http.put<CaseReport>(`${this.base}/${caseId}`, request);
  }

  delete(caseId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${caseId}`);
  }

  evaluate(caseId: string): Observable<CaseReport> {
    return this.http.post<CaseReport>(`${this.base}/${caseId}/evaluate`, {});
  }

  demo(): Observable<CaseReport> {
    return this.http.post<CaseReport>(`${this.base}/demo`, {});
  }

  audit(caseId: string): Observable<AuditRecord[]> {
    return this.http.get<AuditRecord[]>(`${this.base}/${caseId}/audit`);
  }

  readyForInitialReview(caseId: string): Observable<BackwardChainingReport> {
    return this.http.get<BackwardChainingReport>(`${this.base}/${caseId}/queries/ready-for-review`);
  }

  caseProcessable(caseId: string): Observable<BackwardChainingReport> {
    return this.http.get<BackwardChainingReport>(`${this.base}/${caseId}/queries/processable`);
  }
}
