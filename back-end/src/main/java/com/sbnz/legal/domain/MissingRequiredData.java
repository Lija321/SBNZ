package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.MissingDataField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissingRequiredData {
    private String caseId;
    private MissingDataField field;
}
