package com.sbnz.legal.config;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Builds the full production knowledge base (all DRLs + the generated checklist rules) exactly as
 * {@link DroolsConfig} does, to catch cross-file compilation issues such as dangling globals.
 */
class KieBaseBuildTest {

    @Test
    void fullKnowledgeBaseBuildsAndOpensSession() {
        KieContainer container = new DroolsConfig().kieContainer();
        assertNotNull(container);
        KieSession session = container.newKieSession();
        try {
            assertNotNull(session.getSessionClock());
        } finally {
            session.dispose();
        }
    }
}
