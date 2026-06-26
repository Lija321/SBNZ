package com.sbnz.legal.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "case_indicator")
@Getter
@Setter
public class CaseIndicatorEntity {

    @Id
    @Column(name = "case_id", length = 36)
    private String caseId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "case_id")
    private LegalCaseEntity legalCase;

    @Column(name = "has_debt_or_claim", nullable = false)
    private boolean hasDebtOrClaim;

    @Column(name = "has_damage", nullable = false)
    private boolean hasDamage;

    @Column(name = "has_contract", nullable = false)
    private boolean hasContract;

    @Column(name = "has_invoice", nullable = false)
    private boolean hasInvoice;

    @Column(name = "has_real_estate", nullable = false)
    private boolean hasRealEstate;

    @Column(name = "has_cadastre_data", nullable = false)
    private boolean hasCadastreData;

    @Column(name = "has_official_act", nullable = false)
    private boolean hasOfficialAct;

    @Column(name = "claim_amount")
    private BigDecimal claimAmount;
}
