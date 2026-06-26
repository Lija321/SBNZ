package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.ProcedureStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

/**
 * Static domain knowledge: performing {@code step} requires that {@code requiresStep}
 * has been reached first. Edges of the procedural prerequisite graph. Fields are
 * positional so they can be used in positional backward-chaining query patterns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepRequiresStep {

    @Position(0)
    private ProcedureStep step;

    @Position(1)
    private ProcedureStep requiresStep;
}
