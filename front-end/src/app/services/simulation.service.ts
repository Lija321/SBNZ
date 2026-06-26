import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SimulationStatusView {
  simulatedNow: string;
}

@Injectable({ providedIn: 'root' })
export class SimulationService {
  private readonly base = '/api/v1/simulation';

  constructor(private readonly http: HttpClient) {}

  clock(): Observable<SimulationStatusView> {
    return this.http.get<SimulationStatusView>(`${this.base}/clock`);
  }

  advance(days: number): Observable<SimulationStatusView> {
    return this.http.post<SimulationStatusView>(`${this.base}/advance`, { days });
  }

  reset(): Observable<SimulationStatusView> {
    return this.http.post<SimulationStatusView>(`${this.base}/reset`, {});
  }
}
