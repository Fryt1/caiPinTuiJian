package com.aishipin.repository;

import com.aishipin.entity.UserHealthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserHealthInfoRepository extends JpaRepository<UserHealthInfo, Long> {
    
    /**
     * 根据用户ID查询健康信息
     */
    Optional<UserHealthInfo> findByUserId(Long userId);
}
