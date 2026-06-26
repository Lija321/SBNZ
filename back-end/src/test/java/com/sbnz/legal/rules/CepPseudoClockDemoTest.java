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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stand-alone CEP demo intended to be shown live at the defense ("odbrana") using the pseudo clock.
 *
 * It builds a STREAM-mode knowledge base from the real {@code cep_rules.drl} and a pseudo session
 * clock, then advances simulated time one day at a time and fires the rules after each step — so the
 * temporal behaviour (nothing fires while activity is recent, the alert fires once the window
 * elapses) is visible in the console output.
 *
 * Run just this demo with:
 *   ./mvnw -Dtest=CepPseudoClockDemoTest test
 */
class CepPseudoClockDemoTest {

    private static KieContainer container;

    @BeforeAll
    static void buildContainer() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        KieModuleModel module = ks.newKieModuleModel();
        KieBaseModel base = module.newKieBaseModel("cep-demo")
                .setDefault(true)
                .addPackage("rules")
                .setEventProcessingMode(EventProcessingOption.STREAM);
        base.newKieSessionModel("cep-demo-session")
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

    private boolean hasAlert(KieSession session, String caseId, CepAlertType type) {
        return session.getObjects(o -> o instanceof CepAlert a
                && a.getCaseId().equals(caseId) && a.getAlertType() == type).size() > 0;
    }

    /**
     * Scenario 1: a CONTRACT is requested on day 0 but never delivered.
     * The "missing document not added" rule must stay silent for 7 days and fire on day 7.
     */
    @Test
    void demo_missingDocumentAlertAppearsAfterSevenDays() {
        KieSession session = container.newKieSession();
        SessionPseudoClock clock = session.getSessionClock();
        try {
            session.insert(new TaskCreatedEvent("DEMO-1", TaskType.REQUEST_CONTRACT, DocumentType.CONTRACT, 0L));
            session.insert(new MissingRequiredDocument("DEMO-1", DocumentType.CONTRACT));
            System.out.println("[Day 0] CONTRACT requested for case DEMO-1 (no document delivered).");

            int firstAlertDay = -1;
            for (int day = 1; day <= 9; day++) {
                clock.advanceTime(1, TimeUnit.DAYS);
                int fired = session.fireAllRules();
                boolean alert = hasAlert(session, "DEMO-1", CepAlertType.DOCUMENT_NOT_ADDED);
                System.out.printf("[Day %d] rules fired this step: %d | DOCUMENT_NOT_ADDED alert present: %b%n",
                        day, fired, alert);
                if (alert && firstAlertDay == -1) {
                    firstAlertDay = day;
                }
            }

            System.out.println("=> Alert first appeared on day " + firstAlertDay + " (7-day window).");
            assertEquals(7, firstAlertDay, "alert should fire exactly when the 7-day window elapses");
        } finally {
            session.dispose();
        }
    }

    /**
     * Scenario 2: a steady stream of activity keeps the case "active"; once activity stops for
     * 30 days the "old case activity" rule flags it — the same heartbeat idea as the TA example.
     */
    @Test
    void demo_caseGoesInactiveAfterThirtyQuietDays() {
        KieSession session = container.newKieSession();
        SessionPseudoClock clock = session.getSessionClock();
        try {
            session.insert(legalCase("DEMO-2", Instant.EPOCH));
            session.insert(new CaseUpdatedEvent("DEMO-2", 0L));
            System.out.println("[Day 0] case DEMO-2 opened (activity recorded).");

            // 25 quiet days: still inside the 30-day window, so the case stays active.
            clock.advanceTime(25, TimeUnit.DAYS);
            session.fireAllRules();
            boolean inactiveAt25 = session.getObjects(CaseInactive.class::isInstance).size() > 0;
            System.out.printf("[Day 25] CaseInactive present: %b (expected false)%n", inactiveAt25);
            assertTrue(!inactiveAt25, "case should still be active after 25 quiet days");

            // Cross the 30-day boundary.
            clock.advanceTime(6, TimeUnit.DAYS);
            session.fireAllRules();
            boolean inactiveAt31 = session.getObjects(CaseInactive.class::isInstance).size() > 0;
            System.out.printf("[Day 31] CaseInactive present: %b (expected true)%n", inactiveAt31);
            assertTrue(inactiveAt31, "case should be flagged inactive once 30 quiet days pass");
        } finally {
            session.dispose();
        }
    }

    private LegalCase legalCase(String id, Instant lastUpdated) {
        return new LegalCase(id, id, "demo", "user", lastUpdated, lastUpdated, false, CaseType.DEBT_COLLECTION);
    }
}
