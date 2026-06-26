package com.sbnz.legal.demo;

import com.sbnz.legal.domain.CaseInactive;
import com.sbnz.legal.domain.CepAlert;
import com.sbnz.legal.domain.LegalCase;
import com.sbnz.legal.domain.MissingRequiredDocument;
import com.sbnz.legal.domain.cep.CaseUpdatedEvent;
import com.sbnz.legal.domain.cep.TaskCreatedEvent;
import com.sbnz.legal.domain.enums.CaseType;
import com.sbnz.legal.domain.enums.CepAlertType;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.TaskType;
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

/**
 * Stand-alone CEP demonstration driven by the pseudo session clock — no JUnit, no database,
 * no Spring context required. Intended to be shown live at the defense ("odbrana").
 *
 * Run it from the IDE (right-click {@code main}) or from the command line:
 *   ./mvnw -q compile exec:java -Dexec.mainClass=com.sbnz.legal.demo.CepPseudoClockDemo
 *
 * It builds a STREAM-mode knowledge base from the real {@code cep_rules.drl} with a pseudo clock,
 * then advances simulated time and fires the rules after each step, so the temporal behaviour is
 * visible in the console.
 */
public final class CepPseudoClockDemo {

    private CepPseudoClockDemo() {
    }

    public static void main(String[] args) {
        KieContainer container = buildContainer();
        System.out.println("=== CEP pseudo-clock demo =========================================");
        demoMissingDocument(container);
        System.out.println();
        demoCaseInactivity(container);
        System.out.println("===================================================================");
    }

    private static KieContainer buildContainer() {
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
        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    /**
     * A CONTRACT is requested on day 0 but never delivered: the alert must stay silent for 7 days
     * and fire exactly when the {@code after[0s,7d]} window elapses.
     */
    private static void demoMissingDocument(KieContainer container) {
        System.out.println("--- Scenario 1: required document not delivered (7-day window) ---");
        KieSession session = container.newKieSession();
        SessionPseudoClock clock = session.getSessionClock();
        try {
            session.insert(new TaskCreatedEvent("DEMO-1", TaskType.REQUEST_CONTRACT, DocumentType.CONTRACT, 0L));
            session.insert(new MissingRequiredDocument("DEMO-1", DocumentType.CONTRACT));
            System.out.println("[Day 0] CONTRACT requested for case DEMO-1 (no document delivered).");

            for (int day = 1; day <= 9; day++) {
                clock.advanceTime(1, TimeUnit.DAYS);
                int fired = session.fireAllRules();
                boolean alert = hasAlert(session, "DEMO-1", CepAlertType.DOCUMENT_NOT_ADDED);
                System.out.printf("[Day %d] rules fired this step: %d | DOCUMENT_NOT_ADDED alert: %s%n",
                        day, fired, alert ? "RAISED" : "-");
            }
        } finally {
            session.dispose();
        }
    }

    /**
     * Activity is recorded on day 0; once 30 quiet days pass the case is flagged inactive
     * (the heartbeat idea: silence inside a sliding window triggers the rule).
     */
    private static void demoCaseInactivity(KieContainer container) {
        System.out.println("--- Scenario 2: case inactivity (30-day window) ---");
        KieSession session = container.newKieSession();
        SessionPseudoClock clock = session.getSessionClock();
        try {
            session.insert(legalCase("DEMO-2", Instant.EPOCH));
            session.insert(new CaseUpdatedEvent("DEMO-2", 0L));
            System.out.println("[Day 0] case DEMO-2 opened (activity recorded).");

            clock.advanceTime(25, TimeUnit.DAYS);
            session.fireAllRules();
            System.out.printf("[Day 25] case flagged inactive: %s (still inside 30-day window)%n",
                    isInactive(session) ? "YES" : "no");

            clock.advanceTime(6, TimeUnit.DAYS);
            session.fireAllRules();
            System.out.printf("[Day 31] case flagged inactive: %s (30 quiet days elapsed)%n",
                    isInactive(session) ? "YES" : "no");
        } finally {
            session.dispose();
        }
    }

    private static boolean hasAlert(KieSession session, String caseId, CepAlertType type) {
        return session.getObjects(o -> o instanceof CepAlert a
                && a.getCaseId().equals(caseId) && a.getAlertType() == type).size() > 0;
    }

    private static boolean isInactive(KieSession session) {
        return session.getObjects(CaseInactive.class::isInstance).size() > 0;
    }

    private static LegalCase legalCase(String id, Instant lastUpdated) {
        return new LegalCase(id, id, "demo", "user", lastUpdated, lastUpdated, false, CaseType.DEBT_COLLECTION);
    }
}
