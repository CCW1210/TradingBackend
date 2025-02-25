package com.example.tradingbackend.controller;

import com.example.tradingbackend.service.KlineSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final KlineSyncService klineSyncService;

    public TestController(KlineSyncService klineSyncService) {
        this.klineSyncService = klineSyncService;
    }

    @GetMapping("/test-sync")
    public ResponseEntity<String> testSync() {
        klineSyncService.syncKlineData();
        return ResponseEntity.ok("同步觸發");
    }
}
