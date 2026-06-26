package com.sbnz.legal.api;

import com.sbnz.legal.domain.DateFact;
import com.sbnz.legal.domain.Document;
import com.sbnz.legal.domain.Party;
import com.sbnz.legal.domain.enums.CaseType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateCaseRequest {
    private String name;
    private String description;
    private String assignedUser;
    private CaseType initialCaseType;
    private Boolean archived;
    private List<Party> parties = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();
    private List<DateFact> dateFacts = new ArrayList<>();
    private CreateCaseRequest.CaseIndicatorInput indicators = new CreateCaseRequest.CaseIndicatorInput();
}
