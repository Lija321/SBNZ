package com.sbnz.legal.persistence.repository;

import com.sbnz.legal.persistence.entity.CaseTimelineEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseTimelineEventRepository extends JpaRepository<CaseTimelineEventEntity, UUID> {

    List<CaseTimelineEventEntity> findByCaseIdOrderByOccurredAtAsc(String caseId);
}
