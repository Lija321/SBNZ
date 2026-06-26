package com.sbnz.legal.service;

import com.sbnz.legal.api.CaseReport;
import com.sbnz.legal.api.SimulationStatusView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CaseSimulationService {

    private final SimulatedClockService simulatedClockService;
    private final CaseService caseService;

    public CaseSimulationService(SimulatedClockService simulatedClockService, CaseService caseService) {
        this.simulatedClockService = simulatedClockService;
        this.caseService = caseService;
    }

    @Transactional(readOnly = true)
    public SimulationStatusView status() {
        return new SimulationStatusView(simulatedClockService.now());
    }

    @Transactional
    public SimulationStatusView advanceDays(long days) {
        Instant now = simulatedClockService.advanceDays(days);
        caseService.reevaluateAllCases();
        return new SimulationStatusView(now);
    }

    @Transactional
    public SimulationStatusView resetClock() {
        simulatedClockService.resetToRealTime();
        caseService.reevaluateAllCases();
        return new SimulationStatusView(simulatedClockService.now());
    }

    @Transactional
    public List<CaseReport> reevaluateAll() {
        return caseService.reevaluateAllCases();
    }
}
