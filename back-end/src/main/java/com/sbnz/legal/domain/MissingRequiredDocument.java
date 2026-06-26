package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissingRequiredDocument {
    private String caseId;
    private DocumentType documentType;
}
