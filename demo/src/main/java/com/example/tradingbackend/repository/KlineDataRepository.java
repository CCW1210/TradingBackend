// KlineDataRepository.java
package com.example.tradingbackend.repository;

import com.example.tradingbackend.model.KlineData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KlineDataRepository extends JpaRepository<KlineData, Long> {
    // 根據需求增加查詢條件，例如 symbol、時間範圍等
}
