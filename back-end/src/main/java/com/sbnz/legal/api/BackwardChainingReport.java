package com.sbnz.legal.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackwardChainingReport {
    private String caseId;
    private String queryName;
    private boolean satisfied;
    private String summary;
    private List<SubGoalResult> subGoals = new ArrayList<>();
    private List<String> blockers = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubGoalResult {
        private String name;
        private boolean satisfied;
        private String detail;
    }
}
