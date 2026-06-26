package com.sbnz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseIndicator {
    private String caseId;
    private boolean hasDebtOrClaim;
    private boolean hasDamage;
    private boolean hasContract;
    private boolean hasInvoice;
    private boolean hasRealEstate;
    private boolean hasCadastreData;
    private boolean hasOfficialAct;
    private BigDecimal claimAmount;
}
