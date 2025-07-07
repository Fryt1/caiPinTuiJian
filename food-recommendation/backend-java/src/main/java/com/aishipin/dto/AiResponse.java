package com.aishipin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    private String answer;
    private String conversationId;
    private String messageId;
}
