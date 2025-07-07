package com.aishipin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_health_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserHealthInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;
    
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "health_goal")
    private HealthGoal healthGoal;
    
    @Column(name = "taste_preference", length = 50)
    private String tastePreference;
    
    @Column(name = "diet_preference", length = 100)
    private String dietPreference;
    
    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public enum HealthGoal {
        减脂, 增肌, 维持
    }
}
