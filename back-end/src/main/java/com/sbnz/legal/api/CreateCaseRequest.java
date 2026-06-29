package com.sbnz.legal.api;

import com.sbnz.legal.domain.DateFact;
import com.sbnz.legal.domain.Document;
import com.sbnz.legal.domain.Party;
import com.sbnz.legal.domain.enums.CaseType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateCaseRequest {
    private String name;
    private String description;
    private String assignedUser;
    private CaseType initialCaseType;
    private List<Party> parties = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private List<DateFact> dateFacts = new ArrayList<>();
    private CaseIndicatorInput indicators = new CaseIndicatorInput();

    @Data
    public static class CaseIndicatorInput {
        private boolean hasDebtOrClaim;
        private boolean hasDamage;
        private boolean hasContract;
        private boolean hasInvoice;
        private boolean hasRealEstate;
        private boolean hasCadastreData;
        private boolean hasOfficialAct;
        private BigDecimal claimAmount;
    }
}
