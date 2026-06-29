package com.sbnz.legal.service;

import com.sbnz.legal.api.CaseReport;
import com.sbnz.legal.api.RuleFiring;
import com.sbnz.legal.domain.AuditRecord;
import com.sbnz.legal.domain.SuggestedTask;
import com.sbnz.legal.domain.enums.AuditRecordType;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.domain.enums.TaskType;
import com.sbnz.legal.persistence.CasePersistenceMapper;
import com.sbnz.legal.persistence.entity.AuditRecordEntity;
import com.sbnz.legal.persistence.entity.LegalCaseEntity;
import com.sbnz.legal.persistence.repository.AuditRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;
    private final CasePersistenceMapper mapper;
    private final SimulatedClockService simulatedClockService;

    public AuditService(
            AuditRecordRepository auditRecordRepository,
            CasePersistenceMapper mapper,
            SimulatedClockService simulatedClockService
    ) {
        this.auditRecordRepository = auditRecordRepository;
        this.mapper = mapper;
        this.simulatedClockService = simulatedClockService;
    }

    @Transactional
    public void recordEvaluation(LegalCaseEntity entity, CaseReport report) {
        String caseId = entity.getCaseId();
        ProcessingStatus previousStatus = entity.getLastStatus();
        Set<TaskType> previousTasks = entity.getLastOpenTasks() != null
                ? new HashSet<>(entity.getLastOpenTasks())
                : Set.of();

        for (RuleFiring firing : report.getRuleFirings()) {
            saveRecord(
                    caseId,
                    AuditRecordType.RULE_FIRED,
                    firing.getRuleName(),
                    firing.getFiredAt(),
                    firing.getExplanation() != null ? firing.getExplanation() : firing.getRuleName(),
                    null,
                    null,
                    null
            );
        }

        ProcessingStatus currentStatus = report.getSummary().getStatus();
        if (previousStatus == null) {
            saveRecord(
                    caseId,
                    AuditRecordType.STATUS_CHANGED,
                    "Select final case status",
                    report.getEvaluatedAt(),
                    "Initial status: " + currentStatus,
                    null,
                    currentStatus,
                    null
            );
        } else if (previousStatus != currentStatus) {
            saveRecord(
                    caseId,
                    AuditRecordType.STATUS_CHANGED,
                    "Select final case status",
                    report.getEvaluatedAt(),
                    "Status changed from " + previousStatus + " to " + currentStatus,
                    previousStatus,
                    currentStatus,
                    null
            );
        }

        Set<TaskType> currentTasks = report.getSuggestedTasks().stream()
                .filter(SuggestedTask::isOpen)
                .map(SuggestedTask::getTaskType)
                .collect(Collectors.toSet());

        for (TaskType taskType : currentTasks) {
            if (!previousTasks.contains(taskType)) {
                saveRecord(
                        caseId,
                        AuditRecordType.TASK_CREATED,
                        null,
                        report.getEvaluatedAt(),
                        "Suggested task created: " + taskType,
                        null,
                        null,
                        taskType
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> getAuditForCase(String caseId) {
        return auditRecordRepository.findByCaseIdOrderByFiredAtDesc(caseId).stream()
                .map(mapper::toAuditRecord)
                .toList();
    }

    @Transactional
    public void recordCaseUpdated(String caseId) {
        saveRecord(
                caseId,
                AuditRecordType.CASE_UPDATED,
                null,
                simulatedClockService.now(),
                "Case data updated",
                null,
                null,
                null
        );
    }

    @Transactional
    public void recordCaseDeleted(String caseId, String caseName) {
        saveRecord(
                caseId,
                AuditRecordType.CASE_DELETED,
                null,
                simulatedClockService.now(),
                "Case deleted: " + caseName,
                null,
                null,
                null
        );
    }

    @Transactional
    public void deleteAuditForCase(String caseId) {
        auditRecordRepository.deleteByCaseId(caseId);
    }

    private void saveRecord(
            String caseId,
            AuditRecordType recordType,
            String ruleName,
            Instant firedAt,
            String explanation,
            ProcessingStatus previousStatus,
            ProcessingStatus newStatus,
            TaskType taskType
    ) {
        AuditRecordEntity record = new AuditRecordEntity();
        record.setCaseId(caseId);
        record.setRecordType(recordType);
        record.setRuleName(ruleName);
        record.setFiredAt(firedAt != null ? firedAt : simulatedClockService.now());
        record.setExplanation(explanation);
        record.setPreviousStatus(previousStatus);
        record.setNewStatus(newStatus);
        record.setTaskType(taskType);
        auditRecordRepository.save(record);
    }
}
