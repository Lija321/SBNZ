package com.sbnz.legal.service;

import com.sbnz.legal.domain.SuggestedTask;
import com.sbnz.legal.domain.cep.CaseUpdatedEvent;
import com.sbnz.legal.domain.cep.DocumentAddedEvent;
import com.sbnz.legal.domain.cep.TaskCreatedEvent;
import com.sbnz.legal.domain.enums.CaseTimelineEventKind;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;
import com.sbnz.legal.persistence.entity.CaseTimelineEventEntity;
import com.sbnz.legal.persistence.repository.CaseTimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CaseTimelineService {

    private final CaseTimelineEventRepository repository;

    public CaseTimelineService(CaseTimelineEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordCaseOpened(String caseId, Instant at) {
        save(caseId, CaseTimelineEventKind.CASE_UPDATED, null, null, at);
    }

    @Transactional
    public void recordCaseUpdated(String caseId, Instant at) {
        save(caseId, CaseTimelineEventKind.CASE_UPDATED, null, null, at);
    }

    @Transactional
    public void recordDocumentAdded(String caseId, DocumentType documentType, Instant at) {
        save(caseId, CaseTimelineEventKind.DOCUMENT_ADDED, null, documentType, at);
    }

    @Transactional
    public void syncOpenTasks(String caseId, List<SuggestedTask> openTasks, Set<TaskType> previousTasks, Instant at) {
        Set<TaskType> current = openTasks.stream()
                .filter(SuggestedTask::isOpen)
                .map(SuggestedTask::getTaskType)
                .collect(Collectors.toSet());
        for (TaskType taskType : current) {
            if (!previousTasks.contains(taskType)) {
                TaskDocumentMapping.documentForTask(taskType)
                        .ifPresent(doc -> save(caseId, CaseTimelineEventKind.TASK_CREATED, taskType, doc, at));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Object> loadCepEvents(String caseId) {
        List<Object> events = new ArrayList<>();
        for (CaseTimelineEventEntity entity : repository.findByCaseIdOrderByOccurredAtAsc(caseId)) {
            long ts = entity.getOccurredAt().toEpochMilli();
            switch (entity.getEventKind()) {
                case TASK_CREATED -> events.add(new TaskCreatedEvent(
                        caseId,
                        entity.getTaskType(),
                        entity.getDocumentType(),
                        ts
                ));
                case DOCUMENT_ADDED -> events.add(new DocumentAddedEvent(
                        caseId,
                        entity.getDocumentType(),
                        ts
                ));
                case CASE_UPDATED -> events.add(new CaseUpdatedEvent(caseId, ts));
            }
        }
        return events;
    }

    private void save(
            String caseId,
            CaseTimelineEventKind kind,
            TaskType taskType,
            DocumentType documentType,
            Instant at
    ) {
        CaseTimelineEventEntity entity = new CaseTimelineEventEntity();
        entity.setCaseId(caseId);
        entity.setEventKind(kind);
        entity.setTaskType(taskType);
        entity.setDocumentType(documentType);
        entity.setOccurredAt(at);
        repository.save(entity);
    }
}
