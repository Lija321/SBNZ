package com.sbnz.legal.config;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class DroolsConfig {

    private static final String VALIDATION_RULES = "rules/validation_rules.drl";
    private static final String CLASSIFICATION_RULES = "rules/classification_rules.drl";
    private static final String STATUS_RULES = "rules/case_status_rules.drl";
    private static final String ACCUMULATE_RULES = "rules/accumulate_rules.drl";
    private static final String DATE_RULES = "rules/date_rules.drl";
    private static final String CEP_RULES = "rules/cep_rules.drl";
    private static final String PROCEDURE_RULES = "rules/procedure_rules.drl";
    private static final String QUERIES = "rules/queries.drl";
    private static final String DOCUMENT_TEMPLATE_PATH = "rules/document_checklist_template.drt";
    private static final String DOCUMENT_TEMPLATE_DATA_PATH = "rules/document_checklist_data.xlsx";
    private static final int DOCUMENT_DATA_FIRST_ROW = 3;
    private static final int DOCUMENT_DATA_FIRST_COLUMN = 2;
    private static final String GENERATED_DOCUMENT_KFS_PATH = "src/main/resources/rules/generated_document_rules.drl";

    @Bean
    public KieContainer kieContainer() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        KieModuleModel module = ks.newKieModuleModel();
        KieBaseModel base = module.newKieBaseModel("rules-base")
                .setDefault(true)
                .addPackage("rules")
                .setEventProcessingMode(EventProcessingOption.STREAM);
        base.newKieSessionModel("rules-session")
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kfs.writeKModuleXML(module.toXML());
        kfs.write(ks.getResources().newClassPathResource(VALIDATION_RULES));
        kfs.write(ks.getResources().newClassPathResource(CLASSIFICATION_RULES));
        writeGeneratedDocumentRules(ks, kfs);
        kfs.write(ks.getResources().newClassPathResource(ACCUMULATE_RULES));
        kfs.write(ks.getResources().newClassPathResource(DATE_RULES));
        kfs.write(ks.getResources().newClassPathResource(CEP_RULES));
        kfs.write(ks.getResources().newClassPathResource(STATUS_RULES));
        kfs.write(ks.getResources().newClassPathResource(QUERIES));
        kfs.write(ks.getResources().newClassPathResource(PROCEDURE_RULES));

        KieBuilder builder = ks.newKieBuilder(kfs);
        builder.buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Drools build failed:\n" + builder.getResults());
        }
        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
    }

    private void writeGeneratedDocumentRules(KieServices ks, KieFileSystem kfs) {
        InputStream templateStream = getClass().getResourceAsStream("/" + DOCUMENT_TEMPLATE_PATH);
        InputStream dataStream = getClass().getResourceAsStream("/" + DOCUMENT_TEMPLATE_DATA_PATH);
        if (templateStream == null) {
            throw new IllegalStateException("Missing rule template on classpath: " + DOCUMENT_TEMPLATE_PATH);
        }
        if (dataStream == null) {
            throw new IllegalStateException("Missing rule template data source on classpath: " + DOCUMENT_TEMPLATE_DATA_PATH);
        }
        String drl = new ExternalSpreadsheetCompiler()
                .compile(dataStream, templateStream, DOCUMENT_DATA_FIRST_ROW, DOCUMENT_DATA_FIRST_COLUMN);
        kfs.write(
                ks.getResources()
                        .newByteArrayResource(drl.getBytes(StandardCharsets.UTF_8))
                        .setResourceType(ResourceType.DRL)
                        .setSourcePath(GENERATED_DOCUMENT_KFS_PATH)
        );
    }
}
