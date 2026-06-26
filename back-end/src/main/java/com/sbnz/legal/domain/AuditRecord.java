package com.sbnz.legal.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sbnz.legal.domain.enums.AuditRecordType;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.domain.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    private UUID id;
    private String caseId;
    private AuditRecordType recordType;
    private String ruleName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant firedAt;
    private String explanation;
    private ProcessingStatus previousStatus;
    private ProcessingStatus newStatus;
    private TaskType taskType;
}
