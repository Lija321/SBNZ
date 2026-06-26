package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.CepAlertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CepAlert {
    private String caseId;
    private CepAlertType alertType;
    private Instant raisedAt;
}
