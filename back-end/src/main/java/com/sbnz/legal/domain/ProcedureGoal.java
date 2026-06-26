package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

/**
 * Static domain knowledge mapping a case type to its top-level procedural goal
 * (the main legal action), e.g. DEBT_COLLECTION -> FILE_LAWSUIT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureGoal {

    @Position(0)
    private CaseType caseType;

    @Position(1)
    private ProcedureStep step;
}
