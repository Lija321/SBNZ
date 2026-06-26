package com.sbnz.legal.config;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.PartyType;
import com.sbnz.legal.domain.enums.TaskType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentChecklistRegistry {

    private final List<ChecklistEntry> entries = new ArrayList<>();

    public DocumentChecklistRegistry() {
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.ANY, DocumentType.CONTRACT, true, TaskType.REQUEST_CONTRACT));
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.ANY, DocumentType.INVOICE, true, TaskType.REQUEST_INVOICE));
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.ANY, DocumentType.SERVICE_PROOF, true, TaskType.REQUEST_SERVICE_PROOF));
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.ANY, DocumentType.PRE_LAWSUIT_NOTICE, false, TaskType.CHECK_IF_NOTICE_EXISTS));
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.LEGAL_ENTITY, DocumentType.COMPANY_REGISTRATION_DATA, false, TaskType.CHECK_COMPANY_DATA));
        entries.add(new ChecklistEntry(CaseType.DEBT_COLLECTION, PartyType.NATURAL_PERSON, DocumentType.PERSONAL_DATA, false, TaskType.CHECK_PERSONAL_DATA));
        entries.add(new ChecklistEntry(CaseType.DAMAGES, PartyType.ANY, DocumentType.DAMAGE_PROOF, true, TaskType.REQUEST_DAMAGE_PROOF));
        entries.add(new ChecklistEntry(CaseType.DAMAGES, PartyType.ANY, DocumentType.DAMAGE_REPORT, false, TaskType.CHECK_IF_DAMAGE_REPORT_EXISTS));
        entries.add(new ChecklistEntry(CaseType.PROPERTY, PartyType.ANY, DocumentType.CADASTRE_EXTRACT, true, TaskType.REQUEST_CADASTRE_EXTRACT));
        entries.add(new ChecklistEntry(CaseType.PROPERTY, PartyType.ANY, DocumentType.OWNERSHIP_DOCUMENT, true, TaskType.REQUEST_OWNERSHIP_DOCUMENT));
        entries.add(new ChecklistEntry(CaseType.PROPERTY, PartyType.PUBLIC_ENTITY, DocumentType.AUTHORIZATION_OR_DECISION, false, TaskType.CHECK_AUTHORIZATION_DOCUMENT));
    }

    public List<ChecklistEntry> getEntries() {
        return List.copyOf(entries);
    }

    public List<ChecklistEntry> entriesFor(CaseType caseType) {
        return entries.stream().filter(e -> e.getCaseType() == caseType).toList();
    }
}
