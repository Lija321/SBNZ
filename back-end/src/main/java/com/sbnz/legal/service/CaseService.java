package com.sbnz.legal.service;

import com.sbnz.legal.api.CaseReport;
import com.sbnz.legal.api.CreateCaseRequest;
import com.sbnz.legal.api.UpdateCaseRequest;
import com.sbnz.legal.domain.*;
import com.sbnz.legal.domain.enums.DateType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.PartyRole;
import com.sbnz.legal.domain.enums.PartyType;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.domain.enums.TaskType;
import com.sbnz.legal.persistence.CasePersistenceMapper;
import com.sbnz.legal.persistence.entity.DocumentEntity;
import com.sbnz.legal.persistence.entity.LegalCaseEntity;
import com.sbnz.legal.persistence.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRulesEngine rulesEngine;
    private final LegalCaseRepository legalCaseRepository;
    private final CasePersistenceMapper mapper;
    private final AuditService auditService;
    private final CaseTimelineService timelineService;
    private final SimulatedClockService simulatedClockService;

    public CaseService(
            CaseRulesEngine rulesEngine,
            LegalCaseRepository legalCaseRepository,
            CasePersistenceMapper mapper,
            AuditService auditService,
            CaseTimelineService timelineService,
            SimulatedClockService simulatedClockService
    ) {
        this.rulesEngine = rulesEngine;
        this.legalCaseRepository = legalCaseRepository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.timelineService = timelineService;
        this.simulatedClockService = simulatedClockService;
    }

    @Transactional(readOnly = true)
    public List<CaseReport> listCases() {
        List<LegalCaseEntity> entities = legalCaseRepository.findAll();
        return entities.stream()
                .map(entity -> evaluateEntity(entity, entities))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CaseReport> getCase(String caseId) {
        return legalCaseRepository.findById(caseId)
                .map(entity -> evaluateEntity(entity, legalCaseRepository.findAll()));
    }

    @Transactional
    public CaseReport createCase(CreateCaseRequest req) {
        String caseId = UUID.randomUUID().toString();
        Instant now = simulatedClockService.now();
        LegalCaseEntity entity = mapper.fromCreateRequest(req, caseId, now);
        timelineService.recordCaseOpened(caseId, now);
        return evaluateAndPersist(entity);
    }

    @Transactional
    public CaseReport evaluate(String caseId) {
        LegalCaseEntity entity = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        return evaluateAndPersist(entity);
    }

    @Transactional
    public CaseReport updateCase(String caseId, UpdateCaseRequest req) {
        LegalCaseEntity entity = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        Instant now = simulatedClockService.now();
        Set<DocumentType> beforePresent = entity.getDocuments().stream()
                .filter(DocumentEntity::isPresent)
                .map(DocumentEntity::getDocumentType)
                .collect(Collectors.toSet());
        mapper.applyUpdate(entity, req, now);
        if (req.getDocuments() != null) {
            for (Document document : req.getDocuments()) {
                if (document.isPresent() && !beforePresent.contains(document.getDocumentType())) {
                    timelineService.recordDocumentAdded(caseId, document.getDocumentType(), now);
                }
            }
        }
        timelineService.recordCaseUpdated(caseId, now);
        auditService.recordCaseUpdated(caseId);
        return evaluateAndPersist(entity);
    }

    @Transactional
    public void deleteCase(String caseId) {
        LegalCaseEntity entity = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        auditService.recordCaseDeleted(caseId, entity.getName());
        legalCaseRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public UpdateCaseRequest getCaseForEdit(String caseId) {
        LegalCaseEntity entity = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found: " + caseId));
        return mapper.toUpdateRequest(entity);
    }

    @Transactional
    public List<CaseReport> reevaluateAllCases() {
        List<LegalCaseEntity> allCases = legalCaseRepository.findAll();
        return allCases.stream()
                .map(this::evaluateAndPersist)
                .toList();
    }

    @Transactional
    public CaseReport seedDemoCase() {
        CreateCaseRequest req = new CreateCaseRequest();
        req.setName("Naplata potraživanja — demo");
        req.setDescription("Sporno potraživanje po fakturi.");
        req.setAssignedUser("kancelarija");

        Party client = new Party(null, PartyRole.CLIENT, PartyType.LEGAL_ENTITY, "Klijent d.o.o.", "klijent@example.com");
        Party opposing = new Party(null, PartyRole.OPPOSING, PartyType.LEGAL_ENTITY, "Dužnik d.o.o.", null);
        req.setParties(List.of(client, opposing));

        req.setDocuments(List.of(
                new Document(null, DocumentType.INVOICE, true),
                new Document(null, DocumentType.CONTRACT, true),
                new Document(null, DocumentType.SERVICE_PROOF, false)
        ));

        req.setDateFacts(List.of(
                new DateFact(null, DateType.DUE_DATE, java.time.LocalDate.parse("2024-03-15"))
        ));

        CreateCaseRequest.CaseIndicatorInput ind = new CreateCaseRequest.CaseIndicatorInput();
        ind.setHasDebtOrClaim(true);
        ind.setHasInvoice(true);
        ind.setHasContract(true);
        req.setIndicators(ind);

        return createCase(req);
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> getAuditLog(String caseId) {
        if (!legalCaseRepository.existsById(caseId)) {
            throw new NoSuchElementException("Case not found: " + caseId);
        }
        return auditService.getAuditForCase(caseId);
    }

    private CaseReport evaluateAndPersist(LegalCaseEntity entity) {
        CaseReport report = evaluateEntity(entity, legalCaseRepository.findAll());
        persistEvaluationSnapshot(entity, report);
        return report;
    }

    private CaseReport evaluateEntity(LegalCaseEntity entity, List<LegalCaseEntity> allCases) {
        CaseRulesEngine.CaseBundle bundle = mapper.toBundle(entity);
        Instant simulatedNow = simulatedClockService.now();
        List<Object> cepEvents = timelineService.loadCepEvents(entity.getCaseId());
        List<OfficeCaseStatus> peerStatuses = allCases.stream()
                .filter(other -> !other.getCaseId().equals(entity.getCaseId()))
                .map(other -> new OfficeCaseStatus(
                        other.getCaseId(),
                        other.getLastStatus() != null ? other.getLastStatus() : ProcessingStatus.INCOMPLETE
                ))
                .toList();
        return rulesEngine.evaluate(bundle, peerStatuses, simulatedNow, cepEvents);
    }

    private void persistEvaluationSnapshot(LegalCaseEntity entity, CaseReport report) {
        auditService.recordEvaluation(entity, report);
        Set<TaskType> previousTasks = entity.getLastOpenTasks() != null
                ? entity.getLastOpenTasks()
                : Set.of();
        timelineService.syncOpenTasks(
                entity.getCaseId(),
                report.getSuggestedTasks(),
                previousTasks,
                report.getSimulatedNow()
        );
        entity.setLastStatus(report.getSummary().getStatus());
        entity.setLastEvaluatedAt(report.getEvaluatedAt());
        entity.setLastOpenTasks(report.getSuggestedTasks().stream()
                .filter(SuggestedTask::isOpen)
                .map(SuggestedTask::getTaskType)
                .collect(Collectors.toSet()));
        legalCaseRepository.save(entity);
    }
}
