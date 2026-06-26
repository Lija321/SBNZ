package com.sbnz.legal.config;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.PartyType;
import com.sbnz.legal.domain.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChecklistEntry {
    private CaseType caseType;
    private PartyType partyType;
    private DocumentType documentType;
    private boolean required;
    private TaskType taskIfMissing;

    /**
     * Party enum name used by the rule template, or {@code null} for {@link PartyType#ANY}.
     * A null value makes the template drop the optional {@code Party(...)} constraint line.
     */
    public String getPartyConstraint() {
        return partyType == PartyType.ANY ? null : partyType.name();
    }

    /**
     * Fact type the template inserts for this entry: a hard requirement versus a softer
     * "expected" document only nudges a check task.
     */
    public String getMissingFactType() {
        return required ? "MissingRequiredDocument" : "MissingExpectedDocument";
    }
}
