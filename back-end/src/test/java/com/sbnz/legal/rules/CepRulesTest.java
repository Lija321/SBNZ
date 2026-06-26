package com.sbnz.legal.rules;

import com.sbnz.legal.domain.CaseInactive;
import com.sbnz.legal.domain.CepAlert;
import com.sbnz.legal.domain.LegalCase;
import com.sbnz.legal.domain.MissingRequiredDocument;
import com.sbnz.legal.domain.cep.CaseUpdatedEvent;
import com.sbnz.legal.domain.cep.DocumentAddedEvent;
import com.sbnz.legal.domain.cep.TaskCreatedEvent;
import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.CepAlertType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionPseudoClock;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the CEP rules using the real {@code cep_rules.drl} on a STREAM-mode knowledge base
 * driven by a pseudo session clock — i.e. the same temporal model the application uses.
 */
class CepRulesTest {

    private static KieContainer container;

    @BeforeAll
    static void buildContainer() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        KieModuleModel module = ks.newKieModuleModel();
        KieBaseModel base = module.newKieBaseModel("cep-test")
                .setDefault(true)
                .addPackage("rules")
                .setEventProcessingMode(EventProcessingOption.STREAM);
        base.newKieSessionModel("cep-test-session")
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kfs.writeKModuleXML(module.toXML());
        kfs.write(ks.getResources().newClassPathResource("rules/cep_rules.drl"));

        KieBuilder builder = ks.newKieBuilder(kfs);
        builder.buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("CEP rules failed to build:\n" + builder.getResults());
        }
        container = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    private long count(KieSession session, Class<?> type) {
        return session.getObjects(type::isInstance).size();
    }

    @Test
    void missingDocumentNotAddedWithinSevenDaysRaisesAlert() {
        KieSession session = container.newKieSession();
        try {
            session.insert(new TaskCreatedEvent("c1", TaskType.REQUEST_CONTRACT, DocumentType.CONTRACT, 0L));
            session.insert(new MissingRequiredDocument("c1", DocumentType.CONTRACT));

            ((SessionPseudoClock) session.getSessionClock()).advanceTime(8, TimeUnit.DAYS);
            session.fireAllRules();

            long alerts = session.getObjects(o -> o instanceof CepAlert a
                    && a.getCaseId().equals("c1") && a.getAlertType() == CepAlertType.DOCUMENT_NOT_ADDED).size();
            assertEquals(1, alerts, "alert expected once the 7d window elapsed without the document");
        } finally {
            session.dispose();
        }
    }

    @Test
    void documentAddedWithinWindowSuppressesAlert() {
        KieSession session = container.newKieSession();
        try {
            session.insert(new TaskCreatedEvent("c2", TaskType.REQUEST_CONTRACT, DocumentType.CONTRACT, 0L));
            session.insert(new MissingRequiredDocument("c2", DocumentType.CONTRACT));
            long twoDays = TimeUnit.DAYS.toMillis(2);
            session.insert(new DocumentAddedEvent("c2", DocumentType.CONTRACT, twoDays));

            ((SessionPseudoClock) session.getSessionClock()).advanceTime(8, TimeUnit.DAYS);
            session.fireAllRules();

            long alerts = session.getObjects(o -> o instanceof CepAlert a
                    && a.getCaseId().equals("c2") && a.getAlertType() == CepAlertType.DOCUMENT_NOT_ADDED).size();
            assertEquals(0, alerts, "document added inside the 7d window must suppress the alert");
        } finally {
            session.dispose();
        }
    }

    @Test
    void caseInactiveForThirtyDaysIsFlagged() {
        KieSession session = container.newKieSession();
        try {
            session.insert(legalCase("c3", Instant.EPOCH));
            session.insert(new CaseUpdatedEvent("c3", 0L));

            ((SessionPseudoClock) session.getSessionClock()).advanceTime(31, TimeUnit.DAYS);
            session.fireAllRules();

            assertEquals(1, count(session, CaseInactive.class), "no activity within the 30d window should flag the case");
            session.getObjects(o -> o instanceof CaseInactive).stream()
                    .map(o -> (CaseInactive) o)
                    .forEach(ci -> assertTrue(ci.getDaysSinceActivity() >= 30));
        } finally {
            session.dispose();
        }
    }

    @Test
    void recentActivityKeepsCaseActive() {
        KieSession session = container.newKieSession();
        try {
            session.insert(legalCase("c4", Instant.EPOCH));
            session.insert(new CaseUpdatedEvent("c4", 0L));

            ((SessionPseudoClock) session.getSessionClock()).advanceTime(10, TimeUnit.DAYS);
            session.fireAllRules();

            assertFalse(count(session, CaseInactive.class) > 0, "activity inside the 30d window must not flag the case");
        } finally {
            session.dispose();
        }
    }

    private LegalCase legalCase(String id, Instant lastUpdated) {
        return new LegalCase(id, id, "desc", "user", lastUpdated, lastUpdated, false, CaseType.DEBT_COLLECTION);
    }
}
