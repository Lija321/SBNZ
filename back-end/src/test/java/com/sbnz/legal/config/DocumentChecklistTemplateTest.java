package com.sbnz.legal.config;

import com.sbnz.legal.domain.CaseClassification;
import com.sbnz.legal.domain.MissingExpectedDocument;
import com.sbnz.legal.domain.MissingRequiredDocument;
import com.sbnz.legal.domain.Party;
import com.sbnz.legal.domain.SuggestedTask;
import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.ClassificationConfidence;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.PartyRole;
import com.sbnz.legal.domain.enums.PartyType;
import com.sbnz.legal.domain.enums.TaskType;
import org.drools.template.ObjectDataCompiler;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the {@code document_checklist_template.drt} rule template compiles to valid DRL
 * straight from {@link ChecklistEntry} objects (scalar columns only) and behaves correctly:
 * the optional {@code Party(...)} line is dropped for {@link PartyType#ANY} entries, and the
 * required/expected branch selects the right fact type.
 */
class DocumentChecklistTemplateTest {

    private String compileRules() {
        InputStream template = getClass().getResourceAsStream("/rules/document_checklist_template.drt");
        assertNotNull(template, "template must be on the test classpath");
        return new ObjectDataCompiler().compile(new DocumentChecklistRegistry().getEntries(), template);
    }

    private KieSession sessionFor(String drl) {
        KieHelper helper = new KieHelper();
        helper.addContent(drl, ResourceType.DRL);
        return helper.build().newKieSession();
    }

    @Test
    void templateCompilesToValidDrl() {
        String drl = compileRules();
        assertTrue(drl.contains("rule \"DEBT_COLLECTION_missing_CONTRACT"), "expected a generated rule name");
        sessionFor(drl);
    }

    @Test
    void anyPartyEntryFlagsRequiredDocumentWithoutParty() {
        KieSession session = sessionFor(compileRules());
        session.insert(new CaseClassification("c1", CaseType.DEBT_COLLECTION, ClassificationConfidence.HIGH));
        session.fireAllRules();

        long missingContract = session.getObjects(o -> o instanceof MissingRequiredDocument m
                && m.getCaseId().equals("c1") && m.getDocumentType() == DocumentType.CONTRACT).size();
        long requestTask = session.getObjects(o -> o instanceof SuggestedTask t
                && t.getCaseId().equals("c1") && t.getTaskType() == TaskType.REQUEST_CONTRACT).size();

        assertEquals(1, missingContract, "ANY-party required doc should fire with no Party present");
        assertEquals(1, requestTask);
    }

    @Test
    void partyScopedRuleOnlyFiresForMatchingPartyType() {
        KieSession session = sessionFor(compileRules());
        session.insert(new CaseClassification("c2", CaseType.DEBT_COLLECTION, ClassificationConfidence.HIGH));
        session.insert(new Party("c2", PartyRole.CLIENT, PartyType.NATURAL_PERSON, "John Doe", null));
        session.fireAllRules();

        long personalData = session.getObjects(o -> o instanceof MissingExpectedDocument m
                && m.getCaseId().equals("c2") && m.getDocumentType() == DocumentType.PERSONAL_DATA).size();
        long companyData = session.getObjects(o -> o instanceof MissingExpectedDocument m
                && m.getCaseId().equals("c2") && m.getDocumentType() == DocumentType.COMPANY_REGISTRATION_DATA).size();

        assertEquals(1, personalData, "NATURAL_PERSON party should trigger personal-data check");
        assertEquals(0, companyData, "LEGAL_ENTITY-scoped rule must not fire for a natural person");
    }
}
