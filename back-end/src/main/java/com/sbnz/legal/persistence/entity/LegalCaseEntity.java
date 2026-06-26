package com.sbnz.legal.persistence.entity;

import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.domain.enums.TaskType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "legal_case")
@Getter
@Setter
public class LegalCaseEntity {

    @Id
    @Column(name = "case_id", length = 36)
    private String caseId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "assigned_user", nullable = false)
    private String assignedUser;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    private boolean archived;

    @Enumerated(EnumType.STRING)
    @Column(name = "initial_case_type")
    private CaseType initialCaseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_status")
    private ProcessingStatus lastStatus;

    @Column(name = "last_evaluated_at")
    private Instant lastEvaluatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "legal_case_open_tasks", joinColumns = @JoinColumn(name = "case_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private Set<TaskType> lastOpenTasks = new HashSet<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyEntity> parties = new ArrayList<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentEntity> documents = new ArrayList<>();

    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DateFactEntity> dateFacts = new ArrayList<>();

    @OneToOne(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private CaseIndicatorEntity indicator;
}
