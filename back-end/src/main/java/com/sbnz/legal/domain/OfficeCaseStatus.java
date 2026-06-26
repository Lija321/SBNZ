package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeCaseStatus {
    private String caseId;
    private ProcessingStatus status;
}
