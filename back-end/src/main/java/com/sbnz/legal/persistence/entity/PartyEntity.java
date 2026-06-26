package com.sbnz.legal.persistence.entity;

import com.sbnz.legal.domain.enums.PartyRole;
import com.sbnz.legal.domain.enums.PartyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "party")
@Getter
@Setter
public class PartyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCaseEntity legalCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Column(nullable = false)
    private String name;

    private String contact;
}
