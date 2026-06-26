package com.sbnz.legal.controller;

import com.sbnz.legal.api.BackwardChainingReport;
import com.sbnz.legal.api.CaseReport;
import com.sbnz.legal.api.CreateCaseRequest;
import com.sbnz.legal.api.UpdateCaseRequest;
import com.sbnz.legal.domain.AuditRecord;
import com.sbnz.legal.service.BackwardChainingService;
import com.sbnz.legal.service.CaseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/cases")
@CrossOrigin(origins = "*")
public class CaseController {

    private final CaseService caseService;
    private final BackwardChainingService backwardChainingService;

    public CaseController(CaseService caseService, BackwardChainingService backwardChainingService) {
        this.caseService = caseService;
        this.backwardChainingService = backwardChainingService;
    }

    @GetMapping
    public List<CaseReport> list() {
        return caseService.listCases();
    }

    @GetMapping("/{caseId}")
    public CaseReport get(@PathVariable String caseId) {
        return caseService.getCase(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found"));
    }

    @GetMapping("/{caseId}/edit")
    public UpdateCaseRequest getForEdit(@PathVariable String caseId) {
        try {
            return caseService.getCaseForEdit(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public CaseReport create(@RequestBody CreateCaseRequest request) {
        return caseService.createCase(request);
    }

    @PutMapping("/{caseId}")
    public CaseReport update(@PathVariable String caseId, @RequestBody UpdateCaseRequest request) {
        try {
            return caseService.updateCase(caseId, request);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{caseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String caseId) {
        try {
            caseService.deleteCase(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{caseId}/evaluate")
    public CaseReport evaluate(@PathVariable String caseId) {
        try {
            return caseService.evaluate(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/demo")
    public CaseReport demo() {
        return caseService.seedDemoCase();
    }

    @GetMapping("/{caseId}/audit")
    public List<AuditRecord> audit(@PathVariable String caseId) {
        try {
            return caseService.getAuditLog(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{caseId}/queries/ready-for-review")
    public BackwardChainingReport readyForInitialReview(@PathVariable String caseId) {
        try {
            return backwardChainingService.readyForInitialReview(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{caseId}/queries/processable")
    public BackwardChainingReport caseProcessable(@PathVariable String caseId) {
        try {
            return backwardChainingService.caseProcessable(caseId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
