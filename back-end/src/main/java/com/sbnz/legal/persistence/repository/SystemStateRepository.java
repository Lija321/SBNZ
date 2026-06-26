package com.sbnz.legal.persistence.repository;

import com.sbnz.legal.persistence.entity.SystemStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemStateRepository extends JpaRepository<SystemStateEntity, String> {
}
