package com.sbnz.legal.domain.cep;

import com.sbnz.legal.domain.CaseEvent;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;
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
public class TaskCreatedEvent implements CaseEvent {
    private String caseId;
    private TaskType taskType;
    private DocumentType documentType;
    private long ts;
}
