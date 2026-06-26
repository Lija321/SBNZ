package com.sbnz.legal.service;

import com.sbnz.legal.api.BackwardChainingReport;
import com.sbnz.legal.config.ProcedurePrerequisiteRegistry;
import com.sbnz.legal.domain.*;
import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import com.sbnz.legal.domain.enums.TaskType;
import com.sbnz.legal.persistence.CasePersistenceMapper;
import com.sbnz.legal.persistence.entity.LegalCaseEntity;
import com.sbnz.legal.persistence.repository.LegalCaseRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BackwardChainingService {

    private static final Map<ProcedureStep, String> STEP_LABELS = Map.of(
            ProcedureStep.ESTABLISH_CLAIM_BASIS, "Utvrđivanje osnova potraživanja",
            ProcedureStep.SEND_PRELAWSUIT_NOTICE, "Slanje opomene pre tužbe",
            ProcedureStep.FILE_LAWSUIT, "Podnošenje tužbe",
            ProcedureStep.ESTABLISH_DAMAGE_BASIS, "Utvrđivanje osnova štete",
            ProcedureStep.FILE_DAMAGES_CLAIM, "Podnošenje zahteva za naknadu štete",
            ProcedureStep.ESTABLISH_OWNERSHIP, "Utvrđivanje vlasništva",
            ProcedureStep.FILE_PROPERTY_CLAIM, "Podnošenje imovinsko-pravnog zahteva"
    );

    private final KieContainer kieContainer;
    private final LegalCaseRepository legalCaseRepository;
    private final CasePersistenceMapper mapper;
    private final CaseTimelineService timelineService;
    private final SimulatedClockService simulatedClockService;
    private final ProcedurePrerequisiteRegistry prerequisiteRegistry;
    private final int officeWaitingThreshold;
    private final int officeReadyThreshold;

    public BackwardChainingService(
            KieContainer kieContainer,
            LegalCaseRepository legalCaseRepository,
            CasePersistenceMapper mapper,
            CaseTimelineService timelineService,
            SimulatedClockService simulatedClockService,
            ProcedurePrerequisiteRegistry prerequisiteRegistry,
            @Value("${office.load.waitingThreshold:2}") int officeWaitingThreshold,
            @Value("${office.load.readyThreshold:2}") int officeReadyThreshold
    ) {
        this.kieContainer = kieContainer;
        this.legalCaseRepository = legalCaseRepository;
        this.mapper = mapper;
        this.timelineService = timelineService;
        this.simulatedClockService = simulatedClockService;
        this.prerequisiteRegistry = prerequisiteRegistry;
        this.officeWaitingThreshold = officeWaitingThreshold;
        this.officeReadyThreshold = officeReadyThreshold;
    }

    @Transactional(readOnly = true)
    public BackwardChainingReport readyForInitialReview(String caseId) {
        return runQuery(caseId, "isCaseReadyForInitialReview", "Da li je predmet spreman za inicijalni pregled?");
    }

    /**
     * Backward chaining over the procedural prerequisite graph: can the case's main legal
     * action be performed? The goal is recursively decomposed into prerequisite steps and
     * documents via the {@code isStepBlocked}, {@code dependsOnStep} and
     * {@code missingDocumentForGoal} queries.
     */
    @Transactional(readOnly = true)
    public BackwardChainingReport caseProcessable(String caseId) {
        legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));

        BackwardChainingReport report = new BackwardChainingReport();
        report.setCaseId(caseId);
        report.setQueryName("isMainActionPossible");

        try (QuerySession session = openQuerySession(caseId)) {
            CaseType caseType = session.classification(caseId);
            Optional<ProcedureStep> mainGoal = caseType == null
                    ? Optional.empty()
                    : prerequisiteRegistry.mainGoal(caseType);

            if (mainGoal.isEmpty()) {
                report.setSatisfied(false);
                report.setSummary("Glavna pravna radnja nije poznata — predmet još nije klasifikovan.");
                report.getBlockers().add("Predmet nije klasifikovan u poznati tip, pa se cilj ne može razložiti.");
                return report;
            }

            ProcedureStep goal = mainGoal.get();
            boolean blocked = session.stepBlocked(caseId, goal);
            report.setSatisfied(!blocked);

            // Sub-goals: every prerequisite step (transitively), then the goal itself.
            List<ProcedureStep> chain = new ArrayList<>(session.prerequisites(goal));
            chain.add(goal);
            for (ProcedureStep step : chain) {
                boolean stepBlocked = session.stepBlocked(caseId, step);
                report.getSubGoals().add(new BackwardChainingReport.SubGoalResult(
                        step.name(),
                        !stepBlocked,
                        stepLabel(step) + (stepBlocked ? " — nije ispunjeno" : " — ispunjeno")
                ));
            }

            for (DocumentType missing : session.missingDocuments(caseId, goal)) {
                report.getBlockers().add("Nedostaje dokument: " + missing);
            }

            report.setSummary("Da li je moguće preduzeti glavnu pravnu radnju (" + stepLabel(goal) + ")? — "
                    + (blocked ? "Ne." : "Da."));
            return report;
        }
    }

    private String stepLabel(ProcedureStep step) {
        return STEP_LABELS.getOrDefault(step, step.name());
    }

    private BackwardChainingReport runQuery(String caseId, String queryName, String summary) {
        BackwardChainingReport report = new BackwardChainingReport();
        report.setCaseId(caseId);
        report.setQueryName(queryName);
        report.setSummary(summary);

        try (QuerySession session = openQuerySession(caseId)) {
            boolean satisfied = session.hasResults(queryName, caseId);
            report.setSatisfied(satisfied);

            if (!session.hasResults("isBasicDataComplete", caseId)) {
                report.getBlockers().add("Osnovni podaci nisu kompletni");
            }
            if (!session.hasResults("hasClassification", caseId)) {
                report.getBlockers().add("Predmet nije klasifikovan");
            }
            collectMissingDocuments(session, caseId, report.getBlockers());
            collectOpenTasks(session, caseId, report.getBlockers());

            report.getSubGoals().add(new BackwardChainingReport.SubGoalResult(
                    "isBasicDataComplete",
                    session.hasResults("isBasicDataComplete", caseId),
                    session.hasResults("isBasicDataComplete", caseId)
                            ? "Osnovni podaci su kompletni"
                            : "Nedostaju osnovni podaci"
            ));
            report.getSubGoals().add(new BackwardChainingReport.SubGoalResult(
                    "hasClassification",
                    session.hasResults("hasClassification", caseId),
                    session.hasResults("hasClassification", caseId)
                            ? "Predmet je klasifikovan"
                            : "Nedostaje klasifikacija"
            ));
            report.getSubGoals().add(new BackwardChainingReport.SubGoalResult(
                    "noMissingRequiredDocuments",
                    !session.hasResults("missingRequiredDocuments", caseId),
                    session.hasResults("missingRequiredDocuments", caseId)
                            ? "Postoje nedostajući obavezni dokumenti"
                            : "Svi obavezni dokumenti su prisutni"
            ));
            report.getSubGoals().add(new BackwardChainingReport.SubGoalResult(
                    "noOpenBlockingTasks",
                    !session.hasResults("openBlockingTasks", caseId),
                    session.hasResults("openBlockingTasks", caseId)
                            ? "Postoje otvoreni zadaci"
                            : "Nema otvorenih blokirajućih zadataka"
            ));

            report.setSummary(summary + (satisfied ? " — Da." : " — Ne."));
            return report;
        }
    }

    private void collectMissingDocuments(QuerySession session, String caseId, List<String> blockers) {
        QueryResults results = session.results("missingRequiredDocuments", caseId);
        for (QueryResultsRow row : results) {
            DocumentType doc = (DocumentType) row.get("documentType");
            blockers.add("Nedostaje obavezni dokument: " + doc);
        }
    }

    private void collectOpenTasks(QuerySession session, String caseId, List<String> blockers) {
        QueryResults results = session.results("openBlockingTasks", caseId);
        for (QueryResultsRow row : results) {
            TaskType task = (TaskType) row.get("taskType");
            blockers.add("Otvoren zadatak: " + task);
        }
    }

    private QuerySession openQuerySession(String caseId) {
        LegalCaseEntity entity = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        List<LegalCaseEntity> allCases = legalCaseRepository.findAll();
        CaseRulesEngine.CaseBundle bundle = mapper.toBundle(entity);
        Instant simulatedNow = simulatedClockService.now();
        List<Object> cepEvents = timelineService.loadCepEvents(caseId);
        List<OfficeCaseStatus> peerStatuses = allCases.stream()
                .filter(other -> !other.getCaseId().equals(caseId))
                .map(other -> new OfficeCaseStatus(
                        other.getCaseId(),
                        other.getLastStatus() != null ? other.getLastStatus() : com.sbnz.legal.domain.enums.ProcessingStatus.INCOMPLETE
                ))
                .toList();

        KieSession session = kieContainer.newKieSession();
        session.setGlobal("officeWaitingThreshold", officeWaitingThreshold);
        session.setGlobal("officeReadyThreshold", officeReadyThreshold);

        replayCepEvents(session, simulatedNow, cepEvents);
        session.insert(bundle.legalCase());
        for (Party party : bundle.parties()) {
            session.insert(party);
        }
        for (Document document : bundle.documents()) {
            session.insert(document);
        }
        for (DateFact dateFact : bundle.dateFacts()) {
            session.insert(dateFact);
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
        return new QuerySession(session);
    }

    private void replayCepEvents(KieSession session, Instant simulatedNow, List<Object> cepEvents) {
        if (cepEvents == null || cepEvents.isEmpty()) {
            return;
        }
        if (!(session.getSessionClock() instanceof SessionPseudoClock pseudo)) {
            cepEvents.forEach(session::insert);
            return;
        }
        long cursor = 0;
        List<Object> sorted = cepEvents.stream()
                .sorted(java.util.Comparator.comparingLong(this::eventTimestamp))
                .toList();
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

    private static final class QuerySession implements AutoCloseable {
        private final KieSession session;

        private QuerySession(KieSession session) {
            this.session = session;
        }

        boolean hasResults(String queryName, String caseId) {
            return results(queryName, caseId).size() > 0;
        }

        QueryResults results(String queryName, String caseId) {
            return session.getQueryResults(queryName, caseId);
        }

        CaseType classification(String caseId) {
            QueryResults results = session.getQueryResults("hasClassification", caseId);
            for (QueryResultsRow row : results) {
                return (CaseType) row.get("caseType");
            }
            return null;
        }

        boolean stepBlocked(String caseId, ProcedureStep step) {
            return session.getQueryResults("isStepBlocked", caseId, step).size() > 0;
        }

        /** Transitive prerequisite steps of a goal, derived with an unbound (open) variable. */
        List<ProcedureStep> prerequisites(ProcedureStep goal) {
            QueryResults results = session.getQueryResults("dependsOnStep", goal, Variable.v);
            Map<ProcedureStep, Boolean> ordered = new LinkedHashMap<>();
            for (QueryResultsRow row : results) {
                ordered.put((ProcedureStep) row.get("dependency"), Boolean.TRUE);
            }
            return new ArrayList<>(ordered.keySet());
        }

        /** All transitively missing documents for a goal, derived with an unbound (open) variable. */
        List<DocumentType> missingDocuments(String caseId, ProcedureStep goal) {
            QueryResults results = session.getQueryResults("missingDocumentForGoal", caseId, goal, Variable.v);
            Map<DocumentType, Boolean> ordered = new LinkedHashMap<>();
            for (QueryResultsRow row : results) {
                ordered.put((DocumentType) row.get("doc"), Boolean.TRUE);
            }
            return new ArrayList<>(ordered.keySet());
        }

        @Override
        public void close() {
            session.dispose();
        }
    }
}
