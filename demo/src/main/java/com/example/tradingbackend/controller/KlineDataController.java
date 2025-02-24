// KlineDataController.java
package com.example.tradingbackend.controller;

import com.example.tradingbackend.model.KlineData;
import com.example.tradingbackend.repository.KlineDataRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class KlineDataController {

    private final KlineDataRepository repository;

    public KlineDataController(KlineDataRepository repository) {
        this.repository = repository;
    }

    // 範例：查詢所有數據，實際應根據時間區間、symbol 等條件過濾
    @GetMapping("/api/history")
    public List<KlineData> getHistory() {
        return repository.findAll();
    }
}
