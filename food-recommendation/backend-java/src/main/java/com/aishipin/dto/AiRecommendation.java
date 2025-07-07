package com.aishipin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendation {
    private List<RecommendationItem> recommendations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String name;
        private String reason;
    }
}
