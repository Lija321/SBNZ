package com.sbnz.legal.config;

import com.sbnz.legal.domain.ProcedureGoal;
import com.sbnz.legal.domain.StepRequiresDocument;
import com.sbnz.legal.domain.StepRequiresStep;
import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static knowledge base of the procedural prerequisite graph. These facts do not depend
 * on any individual case; they describe, per case type, the main legal action and the
 * steps/documents it (transitively) requires. They are inserted into every Drools session
 * so the recursive backward-chaining queries can derive whether a goal is reachable.
 */
@Component
public class ProcedurePrerequisiteRegistry {

    private static final Map<CaseType, ProcedureStep> MAIN_GOALS = Map.of(
            CaseType.DEBT_COLLECTION, ProcedureStep.FILE_LAWSUIT,
            CaseType.DAMAGES, ProcedureStep.FILE_DAMAGES_CLAIM,
            CaseType.PROPERTY, ProcedureStep.FILE_PROPERTY_CLAIM
    );

    /**
     * The full static graph (goals + step/document prerequisites), inserted into the session.
     * Step names are disjoint across case types, so a single shared set never cross-contaminates
     * the per-goal queries.
     */
    public List<Object> baseFacts() {
        List<Object> facts = new ArrayList<>();

        // Goals
        facts.add(new ProcedureGoal(CaseType.DEBT_COLLECTION, ProcedureStep.FILE_LAWSUIT));
        facts.add(new ProcedureGoal(CaseType.DAMAGES, ProcedureStep.FILE_DAMAGES_CLAIM));
        facts.add(new ProcedureGoal(CaseType.PROPERTY, ProcedureStep.FILE_PROPERTY_CLAIM));

        // Debt collection: FILE_LAWSUIT <- SEND_PRELAWSUIT_NOTICE <- ESTABLISH_CLAIM_BASIS
        facts.add(new StepRequiresStep(ProcedureStep.FILE_LAWSUIT, ProcedureStep.SEND_PRELAWSUIT_NOTICE));
        facts.add(new StepRequiresStep(ProcedureStep.SEND_PRELAWSUIT_NOTICE, ProcedureStep.ESTABLISH_CLAIM_BASIS));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_CLAIM_BASIS, DocumentType.CONTRACT));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_CLAIM_BASIS, DocumentType.INVOICE));
        facts.add(new StepRequiresDocument(ProcedureStep.FILE_LAWSUIT, DocumentType.PRE_LAWSUIT_NOTICE));

        // Damages: FILE_DAMAGES_CLAIM <- ESTABLISH_DAMAGE_BASIS
        facts.add(new StepRequiresStep(ProcedureStep.FILE_DAMAGES_CLAIM, ProcedureStep.ESTABLISH_DAMAGE_BASIS));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_DAMAGE_BASIS, DocumentType.DAMAGE_PROOF));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_DAMAGE_BASIS, DocumentType.DAMAGE_REPORT));

        // Property: FILE_PROPERTY_CLAIM <- ESTABLISH_OWNERSHIP
        facts.add(new StepRequiresStep(ProcedureStep.FILE_PROPERTY_CLAIM, ProcedureStep.ESTABLISH_OWNERSHIP));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_OWNERSHIP, DocumentType.CADASTRE_EXTRACT));
        facts.add(new StepRequiresDocument(ProcedureStep.ESTABLISH_OWNERSHIP, DocumentType.OWNERSHIP_DOCUMENT));

        return facts;
    }

    public Optional<ProcedureStep> mainGoal(CaseType caseType) {
        return Optional.ofNullable(MAIN_GOALS.get(caseType));
    }
}
