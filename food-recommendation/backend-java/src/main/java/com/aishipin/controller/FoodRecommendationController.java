package com.aishipin.controller;

import com.aishipin.dto.FoodResponse;
import com.aishipin.dto.RecommendationRequest;
import com.aishipin.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FoodRecommendationController {
    
    private final RecommendationService recommendationService;
    private final DataSource dataSource;
    
    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // 测试数据库连接
            connection.isValid(5);
            response.put("status", "ok");
            response.put("database", "connected");
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            log.error("数据库连接失败", e);
            response.put("status", "error");
            response.put("database", "disconnected");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取食物推荐
     */
    @PostMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(@RequestBody RecommendationRequest request) {
        try {
            log.info("收到推荐请求: userId={}, filters={}", 
                    request.getUserId(), request.getFilters());
            
            List<FoodResponse> recommendations = recommendationService.getRecommendations(request);
            
            log.info("返回推荐结果: {} 个食物", recommendations.size());
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("获取推荐失败", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取推荐失败");
            error.put("details", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }
}
