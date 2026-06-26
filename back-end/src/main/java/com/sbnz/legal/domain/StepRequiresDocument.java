package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

/**
 * Static domain knowledge: performing {@code step} requires document {@code documentType}
 * to be present in the case. Leaf prerequisites of the procedural graph. Fields are
 * positional for use in positional backward-chaining query patterns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepRequiresDocument {

    @Position(0)
    private ProcedureStep step;

    @Position(1)
    private DocumentType documentType;
}
