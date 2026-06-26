package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.ClassificationConfidence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseClassification {
    private String caseId;
    private CaseType caseType;
    private ClassificationConfidence confidence;
}
