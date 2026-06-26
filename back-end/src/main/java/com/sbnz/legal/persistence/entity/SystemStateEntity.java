package com.sbnz.legal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "system_state")
@Getter
@Setter
public class SystemStateEntity {

    @Id
    @Column(name = "state_key", length = 64)
    private String stateKey;

    @Column(name = "instant_value")
    private Instant instantValue;
}
