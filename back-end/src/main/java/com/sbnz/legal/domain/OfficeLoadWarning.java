package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.OfficeLoadWarningType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeLoadWarning {
    private OfficeLoadWarningType type;
    private int caseCount;
}
