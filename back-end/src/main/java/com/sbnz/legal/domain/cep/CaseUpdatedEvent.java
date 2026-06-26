package com.sbnz.legal.domain.cep;

import com.sbnz.legal.domain.CaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Role(Role.Type.EVENT)
@Timestamp("ts")
@Expires("60d")
public class CaseUpdatedEvent implements CaseEvent {
    private String caseId;
    private long ts;
}
