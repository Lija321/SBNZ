package com.sbnz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseInactive {
    private String caseId;
    private int daysSinceActivity;
}
