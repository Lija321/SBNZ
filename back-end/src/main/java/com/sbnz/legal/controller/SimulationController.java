package com.sbnz.legal.controller;

import com.sbnz.legal.api.AdvanceSimulationRequest;
import com.sbnz.legal.api.SimulationStatusView;
import com.sbnz.legal.service.CaseSimulationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final CaseSimulationService simulationService;

    public SimulationController(CaseSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/clock")
    public SimulationStatusView clock() {
        return simulationService.status();
    }

    @PostMapping("/advance")
    public SimulationStatusView advance(@RequestBody(required = false) AdvanceSimulationRequest request) {
        long days = request != null ? request.getDays() : 7;
        return simulationService.advanceDays(days);
    }

    @PostMapping("/reset")
    public SimulationStatusView reset() {
        return simulationService.resetClock();
    }
}
