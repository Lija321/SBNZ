package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.DocumentationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationChecklistStatus {
    private String caseId;
    private DocumentationStatus status;
}
