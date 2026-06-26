package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedTask {
    private String caseId;
    private TaskType taskType;
    private boolean open;
    private Instant createdAt;

    public SuggestedTask(String caseId, TaskType taskType) {
        this(caseId, taskType, true, Instant.now());
    }
}
