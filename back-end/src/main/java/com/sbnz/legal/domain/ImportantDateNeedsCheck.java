package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.DateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportantDateNeedsCheck {
    private String caseId;
    private DateType dateType;
}
