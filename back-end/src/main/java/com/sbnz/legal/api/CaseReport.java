package com.sbnz.legal.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sbnz.legal.domain.*;
import com.sbnz.legal.domain.enums.DateType;
import com.sbnz.legal.domain.enums.MissingDataField;
import com.sbnz.legal.domain.enums.DocumentType;
import com.sbnz.legal.domain.enums.ProcedureStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseReport {
    private String caseId;
    private LegalCase legalCase;
    private CaseSummaryView summary;
    private List<Party> parties = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private List<MissingDataField> missingFields = new ArrayList<>();
    private List<DocumentType> missingRequiredDocuments = new ArrayList<>();
    private List<DocumentType> missingExpectedDocuments = new ArrayList<>();
    private DocumentationChecklistStatus documentationChecklistStatus;
    private List<CaseStatusCandidate> statusCandidates = new ArrayList<>();
    private List<DateType> importantDatesToCheck = new ArrayList<>();
    private List<OfficeLoadWarning> officeLoadWarnings = new ArrayList<>();
    private List<SuggestedTask> suggestedTasks = new ArrayList<>();
    private List<CepAlert> cepAlerts = new ArrayList<>();
    private CaseInactive caseInactive;
    private CaseClassification classification;
    private ProcedureStep mainActionGoal;
    private Boolean mainActionBlocked;
    private List<RuleFiring> ruleFirings = new ArrayList<>();
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant evaluatedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant simulatedNow;
}
