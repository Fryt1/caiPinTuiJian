package com.aishipin.repository;

import com.aishipin.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    /**
     * 根据食物名称列表查询食物
     */
    List<Food> findByNameIn(List<String> names);
    
    /**
     * 根据食物ID列表查询食物
     */
    @Query("SELECT f FROM Food f WHERE f.id IN :ids")
    List<Food> findByIdIn(@Param("ids") List<Long> ids);
    
    /**
     * 根据类别查询食物
     */
    List<Food> findByCategory(String category);
    
    /**
     * 根据卡路里范围查询食物
     */
    @Query("SELECT f FROM Food f WHERE f.calories BETWEEN :minCalories AND :maxCalories")
    List<Food> findByCaloriesRange(@Param("minCalories") Double minCalories, 
                                   @Param("maxCalories") Double maxCalories);
    
    /**
     * 根据蛋白质含量查询高蛋白食物
     */
    @Query("SELECT f FROM Food f WHERE f.protein >= :minProtein")
    List<Food> findHighProteinFoods(@Param("minProtein") Double minProtein);
    
    /**
     * 根据脂肪含量查询低脂食物
     */
    @Query("SELECT f FROM Food f WHERE f.fat <= :maxFat")
    List<Food> findLowFatFoods(@Param("maxFat") Double maxFat);
    
    /**
     * 模糊匹配食物名称
     */
    List<Food> findByNameContaining(String name);
}
