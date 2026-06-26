import { Component, signal } from '@angular/core';
import { RULE_CATALOG } from '../../models/rule-catalog.data';

@Component({
  selector: 'rule-catalog',
  standalone: true,
  templateUrl: './rule-catalog.component.html',
  styleUrl: './rule-catalog.component.scss',
})
export class RuleCatalogComponent {
  protected readonly sections = RULE_CATALOG;
  protected readonly open = signal(false);

  protected show() {
    this.open.set(true);
  }

  protected hide() {
    this.open.set(false);
  }
}
