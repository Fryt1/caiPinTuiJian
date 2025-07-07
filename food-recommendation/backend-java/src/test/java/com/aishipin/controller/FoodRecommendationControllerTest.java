package com.aishipin.controller;

import com.aishipin.dto.RecommendationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class FoodRecommendationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
    
    @Test
    public void testRecommendations() throws Exception {
        // 创建测试请求
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId("user-123");
        
        RecommendationRequest.RecommendationFilters filters = 
                new RecommendationRequest.RecommendationFilters();
        filters.setHealthGoals(Arrays.asList("lose-weight"));
        filters.setDietPreferences(Arrays.asList("low-fat"));
        filters.setAllergies("无");
        
        request.setFilters(filters);
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
