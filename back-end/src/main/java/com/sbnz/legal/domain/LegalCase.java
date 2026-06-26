package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.CaseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalCase {
    private String caseId;
    private String name;
    private String description;
    private String assignedUser;
    private Instant openedAt;
    private Instant lastUpdatedAt;
    private boolean archived;
    private CaseType initialCaseType;
}
