package com.sbnz.legal.service;

import com.sbnz.legal.api.CaseReport;
import com.sbnz.legal.api.CaseSummaryView;
import com.sbnz.legal.api.RuleFiring;
import com.sbnz.legal.config.ProcedurePrerequisiteRegistry;
import com.sbnz.legal.domain.*;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.DocumentationStatus;
import com.sbnz.legal.domain.enums.MissingDataField;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.drools.WorkingMemoryOps;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionClock;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CaseRulesEngine {

    private final KieContainer kieContainer;
    private final ProcedurePrerequisiteRegistry prerequisiteRegistry;
    private final int officeWaitingThreshold;
    private final int officeReadyThreshold;

    public CaseRulesEngine(
            KieContainer kieContainer,
            ProcedurePrerequisiteRegistry prerequisiteRegistry,
            @Value("${office.load.waitingThreshold:2}") int officeWaitingThreshold,
            @Value("${office.load.readyThreshold:2}") int officeReadyThreshold
    ) {
        this.kieContainer = kieContainer;
        this.prerequisiteRegistry = prerequisiteRegistry;
        this.officeWaitingThreshold = officeWaitingThreshold;
        this.officeReadyThreshold = officeReadyThreshold;
    }

    public CaseReport evaluate(
            CaseBundle bundle,
            List<OfficeCaseStatus> peerStatuses,
            Instant simulatedNow,
            List<Object> cepEvents
    ) {
        KieSession session = kieContainer.newKieSession();
        List<RuleFiring> firings = new ArrayList<>();
        session.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(AfterMatchFiredEvent event) {
                firings.add(new RuleFiring(
                        event.getMatch().getRule().getName(),
                        simulatedNow,
                        event.getMatch().getRule().getName()
                ));
            }
        });

        try {
            session.setGlobal("officeWaitingThreshold", officeWaitingThreshold);
            session.setGlobal("officeReadyThreshold", officeReadyThreshold);

            replayCepEvents(session, simulatedNow, cepEvents);

            session.insert(bundle.legalCase());
            for (Party p : bundle.parties()) {
                session.insert(p);
            }
            for (Document d : bundle.documents()) {
                session.insert(d);
            }
            for (DateFact df : bundle.dateFacts()) {
                session.insert(df);
            }
            for (Object procedureFact : prerequisiteRegistry.baseFacts()) {
                session.insert(procedureFact);
            }
            if (bundle.indicator() != null) {
                session.insert(bundle.indicator());
            }
            for (OfficeCaseStatus peer : peerStatuses) {
                session.insert(peer);
            }

            session.fireAllRules();

            String caseId = bundle.legalCase().getCaseId();

            CaseReport report = new CaseReport();
            report.setCaseId(caseId);
            report.setLegalCase(bundle.legalCase());
            report.setParties(bundle.parties());
            report.setDocuments(bundle.documents());
            report.setRuleFirings(firings);
            report.setEvaluatedAt(simulatedNow);
            report.setSimulatedNow(simulatedNow);

            report.setMissingFields(
                    WorkingMemoryOps.getFacts(session, MissingRequiredData.class).stream()
                            .map(MissingRequiredData::getField)
                            .toList()
            );

            report.setSuggestedTasks(
                    WorkingMemoryOps.getFacts(session, SuggestedTask.class).stream()
                            .filter(t -> caseId.equals(t.getCaseId()))
                            .toList()
            );

            report.setMissingRequiredDocuments(
                    WorkingMemoryOps.getFacts(session, MissingRequiredDocument.class).stream()
                            .filter(m -> caseId.equals(m.getCaseId()))
                            .map(MissingRequiredDocument::getDocumentType)
                            .toList()
            );

            report.setMissingExpectedDocuments(
                    WorkingMemoryOps.getFacts(session, MissingExpectedDocument.class).stream()
                            .filter(m -> caseId.equals(m.getCaseId()))
                            .map(MissingExpectedDocument::getDocumentType)
                            .toList()
            );

            DocumentationChecklistStatus checklistStatus = WorkingMemoryOps
                    .getFacts(session, DocumentationChecklistStatus.class).stream()
                    .filter(d -> caseId.equals(d.getCaseId()))
                    .findFirst()
                    .orElse(new DocumentationChecklistStatus(caseId, DocumentationStatus.COMPLETE));
            report.setDocumentationChecklistStatus(checklistStatus);

            report.setStatusCandidates(
                    WorkingMemoryOps.getFacts(session, CaseStatusCandidate.class).stream()
                            .filter(c -> caseId.equals(c.getCaseId()))
                            .toList()
            );

            report.setImportantDatesToCheck(
                    WorkingMemoryOps.getFacts(session, ImportantDateNeedsCheck.class).stream()
                            .filter(d -> caseId.equals(d.getCaseId()))
                            .map(ImportantDateNeedsCheck::getDateType)
                            .toList()
            );

            report.setCepAlerts(
                    WorkingMemoryOps.getFacts(session, CepAlert.class).stream()
                            .filter(a -> caseId.equals(a.getCaseId()))
                            .toList()
            );

            report.setCaseInactive(
                    WorkingMemoryOps.getFacts(session, CaseInactive.class).stream()
                            .filter(c -> caseId.equals(c.getCaseId()))
                            .findFirst()
                            .orElse(null)
            );

            report.setOfficeLoadWarnings(new ArrayList<>(WorkingMemoryOps.getFacts(session, OfficeLoadWarning.class)));

            CaseStatus status = WorkingMemoryOps.getFacts(session, CaseStatus.class).stream()
                    .filter(s -> caseId.equals(s.getCaseId()))
                    .findFirst()
                    .orElse(new CaseStatus(caseId, ProcessingStatus.INCOMPLETE));

            CaseClassification classification = WorkingMemoryOps.getFacts(session, CaseClassification.class).stream()
                    .filter(c -> caseId.equals(c.getCaseId()))
                    .findFirst()
                    .orElse(null);

            report.setClassification(classification);

            WorkingMemoryOps.getFacts(session, MainActionAssessment.class).stream()
                    .filter(a -> caseId.equals(a.getCaseId()))
                    .findFirst()
                    .ifPresent(a -> {
                        report.setMainActionGoal(a.getGoal());
                        report.setMainActionBlocked(a.isBlocked());
                    });

            report.setSummary(new CaseSummaryView(
                    status.getStatus(),
                    classification != null ? classification.getCaseType() : null,
                    checklistStatus.getStatus()
            ));
            return report;
        } finally {
            session.dispose();
        }
    }

    private void replayCepEvents(KieSession session, Instant simulatedNow, List<Object> cepEvents) {
        if (cepEvents == null || cepEvents.isEmpty()) {
            return;
        }
        SessionClock clock = session.getSessionClock();
        if (!(clock instanceof SessionPseudoClock pseudo)) {
            cepEvents.forEach(session::insert);
            return;
        }
        List<Object> sorted = cepEvents.stream()
                .sorted(Comparator.comparingLong(this::eventTimestamp))
                .toList();
        long cursor = 0;
        for (Object event : sorted) {
            long ts = eventTimestamp(event);
            if (ts > cursor) {
                pseudo.advanceTime(ts - cursor, TimeUnit.MILLISECONDS);
                cursor = ts;
            }
            session.insert(event);
        }
        long nowMs = simulatedNow.toEpochMilli();
        if (nowMs > cursor) {
            pseudo.advanceTime(nowMs - cursor, TimeUnit.MILLISECONDS);
        }
    }

    private long eventTimestamp(Object event) {
        if (event instanceof com.sbnz.legal.domain.cep.TaskCreatedEvent e) {
            return e.getTs();
        }
        if (event instanceof com.sbnz.legal.domain.cep.DocumentAddedEvent e) {
            return e.getTs();
        }
        if (event instanceof com.sbnz.legal.domain.cep.CaseUpdatedEvent e) {
            return e.getTs();
        }
        return 0;
    }

    public record CaseBundle(
            LegalCase legalCase,
            List<Party> parties,
            List<Document> documents,
            List<DateFact> dateFacts,
            CaseIndicator indicator
    ) {}
}
