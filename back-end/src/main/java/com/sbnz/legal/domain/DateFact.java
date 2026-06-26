package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.DateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateFact {
    private String caseId;
    private DateType dateType;
    private LocalDate value;
}
