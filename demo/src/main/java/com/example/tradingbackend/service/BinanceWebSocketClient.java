package com.example.tradingbackend.service;

import com.example.tradingbackend.model.KlineData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * BinanceWebSocketClient 負責連線 Binance WebSocket 並處理回傳的行情數據。
 */
@Slf4j
@Service
public class BinanceWebSocketClient {

    @Value("${binance.ws-url}")
    private String binanceWsUrl;

    private WebSocketClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisStreamService redisStreamService;

    public BinanceWebSocketClient(SimpMessagingTemplate messagingTemplate, RedisStreamService redisStreamService) {
        this.messagingTemplate = messagingTemplate;
        this.redisStreamService = redisStreamService;
    }

    @PostConstruct
    public void init() {
        connect();
    }

    private void connect() {
        try {
            client = new WebSocketClient(new URI(binanceWsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info("Connected to Binance WebSocket");
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(message);
                        // 從 k 線資料中讀取各欄位
                        long timestamp = jsonNode.path("k").path("t").asLong();
                        double open = jsonNode.path("k").path("o").asDouble();
                        double close = jsonNode.path("k").path("c").asDouble();
                        double high = jsonNode.path("k").path("h").asDouble();
                        double low = jsonNode.path("k").path("l").asDouble();
                        double volume = jsonNode.path("k").path("v").asDouble();
                        boolean isClosed = jsonNode.path("k").path("x").asBoolean();

                        KlineData data = new KlineData(timestamp, open, close, low, high, volume);
                        data.setClosed(isClosed);

                        // 廣播數據到前端的 STOMP 頻道 /topic/price
                        messagingTemplate.convertAndSend("/topic/price", data);

                        // 寫入 Redis Streams（會包含 closed 欄位）
                        redisStreamService.addKlineData(data);
                    } catch (Exception e) {
                        log.error("Failed to process Binance message", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Binance WebSocket closed: {} - {}", code, reason);
                    reconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Binance WebSocket error", ex);
                    reconnect();
                }
            };

            client.connect();
        } catch (Exception e) {
            log.error("Error connecting to Binance WebSocket", e);
            reconnect();
        }
    }

    private void reconnect() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Reconnecting to Binance WebSocket...");
        connect();
    }
}
