package com.aishipin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "recommended_food_ids", columnDefinition = "TEXT")
    private String recommendedFoodIds;
    
    @Column(name = "filter_info", columnDefinition = "TEXT")
    private String filterInfo;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
