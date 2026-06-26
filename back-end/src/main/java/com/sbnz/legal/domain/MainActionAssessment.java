package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.ProcedureStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Derived fact produced by a forward rule that invokes the recursive backward-chaining
 * query: tells whether the case's main legal action ({@code goal}) is currently blocked
 * by missing prerequisites.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainActionAssessment {
    private String caseId;
    private ProcedureStep goal;
    private boolean blocked;
}
