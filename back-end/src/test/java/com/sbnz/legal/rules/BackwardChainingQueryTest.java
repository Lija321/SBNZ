package com.sbnz.legal.rules;

import com.sbnz.legal.domain.Document;
import com.sbnz.legal.domain.StepRequiresDocument;
import com.sbnz.legal.domain.StepRequiresStep;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the recursive backward-chaining queries from {@code queries.drl} over the
 * procedural prerequisite graph for debt collection:
 *   FILE_LAWSUIT  <- SEND_PRELAWSUIT_NOTICE <- ESTABLISH_CLAIM_BASIS[CONTRACT, INVOICE]
 *   FILE_LAWSUIT  requires document PRE_LAWSUIT_NOTICE
 * Covers blocked/unblocked goals, transitive propagation of a missing leaf document,
 * open-variable enumeration of the prerequisite chain and of all missing documents.
 */
class BackwardChainingQueryTest {

    private static final String CID = "c1";
    private static KieContainer container;

    @BeforeAll
    static void build() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write(ks.getResources().newClassPathResource("rules/queries.drl"));
        KieBuilder builder = ks.newKieBuilder(kfs);
        builder.buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("queries.drl failed to build:\n" + builder.getResults());
        }
        container = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    private void insertDebtGraph(KieSession session) {
        session.insert(new StepRequiresStep(ProcedureStep.FILE_LAWSUIT, ProcedureStep.SEND_PRELAWSUIT_NOTICE));
        session.insert(new StepRequiresStep(ProcedureStep.SEND_PRELAWSUIT_NOTICE, ProcedureStep.ESTABLISH_CLAIM_BASIS));
        session.insert(new StepRequiresDocument(ProcedureStep.ESTABLISH_CLAIM_BASIS, DocumentType.CONTRACT));
        session.insert(new StepRequiresDocument(ProcedureStep.ESTABLISH_CLAIM_BASIS, DocumentType.INVOICE));
        session.insert(new StepRequiresDocument(ProcedureStep.FILE_LAWSUIT, DocumentType.PRE_LAWSUIT_NOTICE));
    }

    private boolean blocked(KieSession session, ProcedureStep goal) {
        return session.getQueryResults("isStepBlocked", CID, goal).size() > 0;
    }

    private Set<ProcedureStep> prerequisites(KieSession session, ProcedureStep goal) {
        QueryResults results = session.getQueryResults("dependsOnStep", goal, Variable.v);
        Set<ProcedureStep> steps = new HashSet<>();
        for (QueryResultsRow row : results) {
            steps.add((ProcedureStep) row.get("dependency"));
        }
        return steps;
    }

    private Set<DocumentType> missingDocuments(KieSession session, ProcedureStep goal) {
        QueryResults results = session.getQueryResults("missingDocumentForGoal", CID, goal, Variable.v);
        Set<DocumentType> docs = new HashSet<>();
        for (QueryResultsRow row : results) {
            docs.add((DocumentType) row.get("doc"));
        }
        return docs;
    }

    @Test
    void goalIsReachableWhenAllDocumentsPresent() {
        KieSession session = container.newKieSession();
        try {
            insertDebtGraph(session);
            session.insert(new Document(CID, DocumentType.CONTRACT, true));
            session.insert(new Document(CID, DocumentType.INVOICE, true));
            session.insert(new Document(CID, DocumentType.PRE_LAWSUIT_NOTICE, true));

            assertFalse(blocked(session, ProcedureStep.ESTABLISH_CLAIM_BASIS));
            assertFalse(blocked(session, ProcedureStep.SEND_PRELAWSUIT_NOTICE));
            assertFalse(blocked(session, ProcedureStep.FILE_LAWSUIT), "all prerequisites met -> goal reachable");
            assertTrue(missingDocuments(session, ProcedureStep.FILE_LAWSUIT).isEmpty());
        } finally {
            session.dispose();
        }
    }

    @Test
    void onlyGoalLevelDocumentMissingBlocksGoalButNotPrerequisites() {
        KieSession session = container.newKieSession();
        try {
            insertDebtGraph(session);
            session.insert(new Document(CID, DocumentType.CONTRACT, true));
            session.insert(new Document(CID, DocumentType.INVOICE, true));
            // PRE_LAWSUIT_NOTICE absent

            assertFalse(blocked(session, ProcedureStep.ESTABLISH_CLAIM_BASIS), "basis docs present");
            assertFalse(blocked(session, ProcedureStep.SEND_PRELAWSUIT_NOTICE));
            assertTrue(blocked(session, ProcedureStep.FILE_LAWSUIT), "missing PRE_LAWSUIT_NOTICE blocks filing");
            assertEquals(Set.of(DocumentType.PRE_LAWSUIT_NOTICE), missingDocuments(session, ProcedureStep.FILE_LAWSUIT));
        } finally {
            session.dispose();
        }
    }

    @Test
    void missingLeafDocumentPropagatesUpTheChain() {
        KieSession session = container.newKieSession();
        try {
            insertDebtGraph(session);
            session.insert(new Document(CID, DocumentType.CONTRACT, true));
            // INVOICE absent
            session.insert(new Document(CID, DocumentType.PRE_LAWSUIT_NOTICE, true));

            assertTrue(blocked(session, ProcedureStep.ESTABLISH_CLAIM_BASIS), "missing INVOICE blocks the basis step");
            assertTrue(blocked(session, ProcedureStep.SEND_PRELAWSUIT_NOTICE), "block propagates transitively");
            assertTrue(blocked(session, ProcedureStep.FILE_LAWSUIT), "block propagates to the top goal");
            assertEquals(Set.of(DocumentType.INVOICE), missingDocuments(session, ProcedureStep.FILE_LAWSUIT));
        } finally {
            session.dispose();
        }
    }

    @Test
    void openVariableEnumeratesFullPrerequisiteChain() {
        KieSession session = container.newKieSession();
        try {
            insertDebtGraph(session);

            assertEquals(
                    Set.of(ProcedureStep.SEND_PRELAWSUIT_NOTICE, ProcedureStep.ESTABLISH_CLAIM_BASIS),
                    prerequisites(session, ProcedureStep.FILE_LAWSUIT),
                    "transitive closure must include both direct and indirect prerequisites"
            );
            assertEquals(
                    Set.of(ProcedureStep.ESTABLISH_CLAIM_BASIS),
                    prerequisites(session, ProcedureStep.SEND_PRELAWSUIT_NOTICE)
            );
        } finally {
            session.dispose();
        }
    }
}
