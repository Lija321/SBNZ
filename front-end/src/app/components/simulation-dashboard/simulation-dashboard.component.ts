import { DatePipe } from '@angular/common';
import { Component, computed, input, output, signal } from '@angular/core';

@Component({
  selector: 'simulation-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './simulation-dashboard.component.html',
  styleUrl: './simulation-dashboard.component.scss',
})
export class SimulationDashboardComponent {
  readonly simulatedNow = input<string | null>(null);
  readonly busy = input(false);

  readonly advanceDays = output<number>();
  readonly resetClock = output<void>();

  private readonly realNow = signal(new Date());

  protected readonly offsetDays = computed(() => {
    const sim = this.simulatedNow();
    if (!sim) return 0;
    const diffMs = new Date(sim).getTime() - this.realNow().getTime();
    return Math.round(diffMs / (24 * 60 * 60 * 1000));
  });
}
