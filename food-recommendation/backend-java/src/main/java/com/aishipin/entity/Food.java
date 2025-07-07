package com.aishipin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "calories", precision = 10, scale = 2)
    private BigDecimal calories;
    
    @Column(name = "protein", precision = 10, scale = 2)
    private BigDecimal protein;
    
    @Column(name = "fat", precision = 10, scale = 2)
    private BigDecimal fat;
    
    @Column(name = "carbohydrate", precision = 10, scale = 2)
    private BigDecimal carbohydrate;
    
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
