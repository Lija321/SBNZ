package com.sbnz.legal.persistence.repository;

import com.sbnz.legal.persistence.entity.LegalCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalCaseRepository extends JpaRepository<LegalCaseEntity, String> {
}
