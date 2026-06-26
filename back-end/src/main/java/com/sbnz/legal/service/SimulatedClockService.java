package com.sbnz.legal.service;

import com.sbnz.legal.persistence.entity.SystemStateEntity;
import com.sbnz.legal.persistence.repository.SystemStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SimulatedClockService {

    private static final String SIMULATED_NOW_KEY = "simulated_now";

    private final SystemStateRepository repository;

    public SimulatedClockService(SystemStateRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Instant now() {
        return repository.findById(SIMULATED_NOW_KEY)
                .map(SystemStateEntity::getInstantValue)
                .orElseGet(this::initializeNow);
    }

    @Transactional
    public Instant advanceDays(long days) {
        Instant current = now();
        Instant advanced = current.plusSeconds(days * 24 * 60 * 60);
        persist(advanced);
        return advanced;
    }

    @Transactional
    public void resetToRealTime() {
        persist(Instant.now());
    }

    private Instant initializeNow() {
        Instant now = Instant.now();
        persist(now);
        return now;
    }

    private void persist(Instant value) {
        SystemStateEntity state = repository.findById(SIMULATED_NOW_KEY).orElseGet(() -> {
            SystemStateEntity created = new SystemStateEntity();
            created.setStateKey(SIMULATED_NOW_KEY);
            return created;
        });
        state.setInstantValue(value);
        repository.save(state);
    }
}
