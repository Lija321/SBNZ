package com.sbnz.legal.api;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.DocumentationStatus;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseSummaryView {
    private ProcessingStatus status;
    private CaseType classification;
    private DocumentationStatus documentation;
}
