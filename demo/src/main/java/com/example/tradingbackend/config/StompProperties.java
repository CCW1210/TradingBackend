package com.example.tradingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 此類別用來綁定 application.properties 中以 "stomp" 為前綴的屬性
 * 例如: stomp.endpoint=/ws
 * 基於2025年的最新最佳實踐設計
 */
@Data
@Component
@ConfigurationProperties(prefix = "stomp")
public class StompProperties {
    private String endpoint;
}
