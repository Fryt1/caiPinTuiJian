package com.aishipin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    
    private Map<String, Object> inputs;
    private String query;
    private String responseMode = "blocking";
    private String user;
}
