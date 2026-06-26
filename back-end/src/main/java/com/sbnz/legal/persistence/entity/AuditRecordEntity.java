package com.sbnz.legal.persistence.entity;

import com.sbnz.legal.domain.enums.AuditRecordType;
import com.sbnz.legal.domain.enums.ProcessingStatus;
import com.sbnz.legal.domain.enums.TaskType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_record", indexes = {
        @Index(name = "idx_audit_case_id", columnList = "case_id"),
        @Index(name = "idx_audit_fired_at", columnList = "fired_at")
})
@Getter
@Setter
public class AuditRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private AuditRecordType recordType;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(length = 2000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private ProcessingStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private ProcessingStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;
}
