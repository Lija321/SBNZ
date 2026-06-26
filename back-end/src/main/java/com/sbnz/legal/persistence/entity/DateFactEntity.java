package com.sbnz.legal.persistence.entity;

import com.sbnz.legal.domain.enums.DateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "date_fact")
@Getter
@Setter
public class DateFactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCaseEntity legalCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_type", nullable = false)
    private DateType dateType;

    @Column(nullable = false)
    private LocalDate value;
}
