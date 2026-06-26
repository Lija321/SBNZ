package com.sbnz.legal.service;

import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;

import java.util.Map;
import java.util.Optional;

public final class TaskDocumentMapping {

    private static final Map<TaskType, DocumentType> TASK_TO_DOCUMENT = Map.of(
            TaskType.REQUEST_CONTRACT, DocumentType.CONTRACT,
            TaskType.REQUEST_INVOICE, DocumentType.INVOICE,
            TaskType.REQUEST_SERVICE_PROOF, DocumentType.SERVICE_PROOF,
            TaskType.REQUEST_DAMAGE_PROOF, DocumentType.DAMAGE_PROOF,
            TaskType.REQUEST_CADASTRE_EXTRACT, DocumentType.CADASTRE_EXTRACT,
            TaskType.REQUEST_OWNERSHIP_DOCUMENT, DocumentType.OWNERSHIP_DOCUMENT
    );

    private TaskDocumentMapping() {
    }

    public static Optional<DocumentType> documentForTask(TaskType taskType) {
        return Optional.ofNullable(TASK_TO_DOCUMENT.get(taskType));
    }
}
