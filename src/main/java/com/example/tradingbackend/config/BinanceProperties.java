package com.example.tradingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 此類別用來綁定 application.properties 中以 "binance" 為前綴的屬性
 * 例如: binance.ws-url=wss://stream.binance.com:9443/ws/btcusdt@kline_1m
 * 屬性名稱會自動轉換為 camelCase (ws-url -> wsUrl)
 * 基於2025年的最新最佳實踐設計
 */
@Data
@Component
@ConfigurationProperties(prefix = "binance")
public class BinanceProperties {
    private String wsUrl;
}
