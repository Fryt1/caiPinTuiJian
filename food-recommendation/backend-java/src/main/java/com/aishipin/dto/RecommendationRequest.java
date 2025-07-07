package com.aishipin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    private String userId;
    private RecommendationFilters filters;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationFilters {
        private String healthGoal;
        private List<String> healthGoals;
        private String dietPreference;
        private List<String> dietPreferences;
        private String allergies;
    }
}
