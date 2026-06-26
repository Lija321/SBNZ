export type RuleCatalogSectionId = 'L1' | 'L2' | 'L3' | 'L4' | 'L5' | 'CEP' | 'DATES';

export interface RuleCatalogEntry {
  name: string;
  condition: string;
  outcome: string;
  fromTemplate?: boolean;
}

export interface RuleCatalogSection {
  id: RuleCatalogSectionId;
  label: string;
  rules: RuleCatalogEntry[];
}

export const RULE_CATALOG_SECTION_ORDER: RuleCatalogSectionId[] = [
  'L1',
  'L2',
  'L3',
  'L4',
  'L5',
  'CEP',
  'DATES',
];
