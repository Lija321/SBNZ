package com.sbnz.legal.persistence.repository;

import com.sbnz.legal.persistence.entity.AuditRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditRecordRepository extends JpaRepository<AuditRecordEntity, UUID> {

    List<AuditRecordEntity> findByCaseIdOrderByFiredAtDesc(String caseId);

    void deleteByCaseId(String caseId);
}
