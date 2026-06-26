package com.sbnz.legal.domain;

import com.sbnz.legal.domain.enums.PartyRole;
import com.sbnz.legal.domain.enums.PartyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Party {
    private String caseId;
    private PartyRole role;
    private PartyType partyType;
    private String name;
    private String contact;
}
