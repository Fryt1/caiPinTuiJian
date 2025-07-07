package com.aishipin.repository;

import com.aishipin.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    
    /**
     * 根据用户ID查询推荐历史
     */
    List<RecommendationHistory> findByUserId(Long userId);
}
