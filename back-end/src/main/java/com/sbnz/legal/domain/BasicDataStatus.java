package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.BasicDataCompleteness;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicDataStatus {
    private String caseId;
    private BasicDataCompleteness status;
}
