package com.sbnz.legal.persistence.entity;

import com.sbnz.legal.domain.enums.CaseTimelineEventKind;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "case_timeline_event", indexes = {
        @Index(name = "idx_timeline_case_id", columnList = "case_id"),
        @Index(name = "idx_timeline_occurred_at", columnList = "occurred_at")
})
@Getter
@Setter
public class CaseTimelineEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_kind", nullable = false)
    private CaseTimelineEventKind eventKind;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
