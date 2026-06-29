package com.sbnz.legal.persistence;

import com.sbnz.legal.api.CreateCaseRequest;
import com.sbnz.legal.api.UpdateCaseRequest;
import com.sbnz.legal.domain.*;
import com.sbnz.legal.persistence.entity.*;
import com.sbnz.legal.service.CaseRulesEngine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class CasePersistenceMapper {

    public CaseRulesEngine.CaseBundle toBundle(LegalCaseEntity entity) {
        LegalCase legalCase = new LegalCase(
                entity.getCaseId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAssignedUser(),
                entity.getOpenedAt(),
                entity.getLastUpdatedAt(),
                entity.isArchived(),
                entity.getInitialCaseType()
        );

        List<Party> parties = entity.getParties().stream()
                .map(p -> new Party(
                        entity.getCaseId(),
                        p.getRole(),
                        p.getPartyType(),
                        p.getName(),
                        p.getContact()
                ))
                .toList();

        List<Document> documents = entity.getDocuments().stream()
                .map(d -> new Document(
                        entity.getCaseId(),
                        d.getDocumentType(),
                        d.isPresent()
                ))
                .toList();

        List<DateFact> dateFacts = entity.getDateFacts().stream()
                .map(df -> new DateFact(
                        entity.getCaseId(),
                        df.getDateType(),
                        df.getValue()
                ))
                .toList();

        CaseIndicator indicator = null;
        if (entity.getIndicator() != null) {
            CaseIndicatorEntity in = entity.getIndicator();
            indicator = new CaseIndicator(
                    entity.getCaseId(),
                    in.isHasDebtOrClaim(),
                    in.isHasDamage(),
                    in.isHasContract(),
                    in.isHasInvoice(),
                    in.isHasRealEstate(),
                    in.isHasCadastreData(),
                    in.isHasOfficialAct(),
                    in.getClaimAmount()
            );
        }

        return new CaseRulesEngine.CaseBundle(legalCase, parties, documents, dateFacts, indicator);
    }

    public LegalCaseEntity fromCreateRequest(CreateCaseRequest req, String caseId, Instant now) {
        LegalCaseEntity entity = new LegalCaseEntity();
        entity.setCaseId(caseId);
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setAssignedUser(req.getAssignedUser());
        entity.setOpenedAt(now);
        entity.setLastUpdatedAt(now);
        entity.setArchived(false);
        entity.setInitialCaseType(req.getInitialCaseType());

        if (req.getParties() != null) {
            for (Party party : req.getParties()) {
                PartyEntity pe = new PartyEntity();
                pe.setLegalCase(entity);
                pe.setRole(party.getRole());
                pe.setPartyType(party.getPartyType());
                pe.setName(party.getName());
                pe.setContact(party.getContact());
                entity.getParties().add(pe);
            }
        }

        if (req.getDocuments() != null) {
            for (Document document : req.getDocuments()) {
                DocumentEntity de = new DocumentEntity();
                de.setLegalCase(entity);
                de.setDocumentType(document.getDocumentType());
                de.setPresent(document.isPresent());
                entity.getDocuments().add(de);
            }
        }

        if (req.getDateFacts() != null) {
            for (DateFact dateFact : req.getDateFacts()) {
                DateFactEntity dfe = new DateFactEntity();
                dfe.setLegalCase(entity);
                dfe.setDateType(dateFact.getDateType());
                dfe.setValue(dateFact.getValue());
                entity.getDateFacts().add(dfe);
            }
        }

        if (req.getIndicators() != null) {
            CreateCaseRequest.CaseIndicatorInput in = req.getIndicators();
            CaseIndicatorEntity indicator = new CaseIndicatorEntity();
            indicator.setCaseId(caseId);
            indicator.setLegalCase(entity);
            indicator.setHasDebtOrClaim(in.isHasDebtOrClaim());
            indicator.setHasDamage(in.isHasDamage());
            indicator.setHasContract(in.isHasContract());
            indicator.setHasInvoice(in.isHasInvoice());
            indicator.setHasRealEstate(in.isHasRealEstate());
            indicator.setHasCadastreData(in.isHasCadastreData());
            indicator.setHasOfficialAct(in.isHasOfficialAct());
            indicator.setClaimAmount(in.getClaimAmount());
            entity.setIndicator(indicator);
        }

        return entity;
    }

    public void applyUpdate(LegalCaseEntity entity, UpdateCaseRequest req, Instant now) {
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setAssignedUser(req.getAssignedUser());
        entity.setInitialCaseType(req.getInitialCaseType());
        entity.setLastUpdatedAt(now);
        if (req.getArchived() != null) {
            entity.setArchived(req.getArchived());
        }

        entity.getParties().clear();
        if (req.getParties() != null) {
            for (Party party : req.getParties()) {
                PartyEntity pe = new PartyEntity();
                pe.setLegalCase(entity);
                pe.setRole(party.getRole());
                pe.setPartyType(party.getPartyType());
                pe.setName(party.getName());
                pe.setContact(party.getContact());
                entity.getParties().add(pe);
            }
        }

        entity.getDocuments().clear();
        if (req.getDocuments() != null) {
            for (Document document : req.getDocuments()) {
                DocumentEntity de = new DocumentEntity();
                de.setLegalCase(entity);
                de.setDocumentType(document.getDocumentType());
                de.setPresent(document.isPresent());
                entity.getDocuments().add(de);
            }
        }

        entity.getDateFacts().clear();
        if (req.getDateFacts() != null) {
            for (DateFact dateFact : req.getDateFacts()) {
                DateFactEntity dfe = new DateFactEntity();
                dfe.setLegalCase(entity);
                dfe.setDateType(dateFact.getDateType());
                dfe.setValue(dateFact.getValue());
                entity.getDateFacts().add(dfe);
            }
        }

        if (req.getIndicators() != null) {
            CreateCaseRequest.CaseIndicatorInput in = req.getIndicators();
            CaseIndicatorEntity indicator = entity.getIndicator();
            if (indicator == null) {
                indicator = new CaseIndicatorEntity();
                indicator.setCaseId(entity.getCaseId());
                indicator.setLegalCase(entity);
                entity.setIndicator(indicator);
            }
            indicator.setHasDebtOrClaim(in.isHasDebtOrClaim());
            indicator.setHasDamage(in.isHasDamage());
            indicator.setHasContract(in.isHasContract());
            indicator.setHasInvoice(in.isHasInvoice());
            indicator.setHasRealEstate(in.isHasRealEstate());
            indicator.setHasCadastreData(in.isHasCadastreData());
            indicator.setHasOfficialAct(in.isHasOfficialAct());
            indicator.setClaimAmount(in.getClaimAmount());
        } else {
            entity.setIndicator(null);
        }
    }

    public UpdateCaseRequest toUpdateRequest(LegalCaseEntity entity) {
        UpdateCaseRequest req = new UpdateCaseRequest();
        req.setName(entity.getName());
        req.setDescription(entity.getDescription());
        req.setAssignedUser(entity.getAssignedUser());
        req.setInitialCaseType(entity.getInitialCaseType());
        req.setArchived(entity.isArchived());
        req.setParties(entity.getParties().stream()
                .map(p -> new Party(
                        entity.getCaseId(),
                        p.getRole(),
                        p.getPartyType(),
                        p.getName(),
                        p.getContact()
                ))
                .toList());
        req.setDocuments(entity.getDocuments().stream()
                .map(d -> new Document(
                        entity.getCaseId(),
                        d.getDocumentType(),
                        d.isPresent()
                ))
                .toList());
        req.setDateFacts(entity.getDateFacts().stream()
                .map(df -> new DateFact(
                        entity.getCaseId(),
                        df.getDateType(),
                        df.getValue()
                ))
                .toList());
        if (entity.getIndicator() != null) {
            CaseIndicatorEntity in = entity.getIndicator();
            CreateCaseRequest.CaseIndicatorInput ind = new CreateCaseRequest.CaseIndicatorInput();
            ind.setHasDebtOrClaim(in.isHasDebtOrClaim());
            ind.setHasDamage(in.isHasDamage());
            ind.setHasContract(in.isHasContract());
            ind.setHasInvoice(in.isHasInvoice());
            ind.setHasRealEstate(in.isHasRealEstate());
            ind.setHasCadastreData(in.isHasCadastreData());
            ind.setHasOfficialAct(in.isHasOfficialAct());
            ind.setClaimAmount(in.getClaimAmount());
            req.setIndicators(ind);
        }
        return req;
    }

    public AuditRecord toAuditRecord(AuditRecordEntity entity) {
        return new AuditRecord(
                entity.getId(),
                entity.getCaseId(),
                entity.getRecordType(),
                entity.getRuleName(),
                entity.getFiredAt(),
                entity.getExplanation(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getTaskType()
        );
    }
}
