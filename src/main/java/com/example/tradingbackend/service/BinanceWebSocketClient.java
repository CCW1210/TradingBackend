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
                        // 解析 JSON 資料，取出 k 線資訊
                        JsonNode jsonNode = objectMapper.readTree(message);
                        JsonNode klineNode = jsonNode.path("k");

                        long timestamp = klineNode.path("t").asLong();
                        double open = klineNode.path("o").asDouble();
                        double close = klineNode.path("c").asDouble();
                        double high = klineNode.path("h").asDouble();
                        double low = klineNode.path("l").asDouble();
                        double volume = klineNode.path("v").asDouble();
                        // 直接根據幣安傳入的 x 欄位判斷是否收盤
                        boolean isClosed = klineNode.path("x").asBoolean();

                        // 建立 KlineData 並設置 closed 狀態
                        KlineData data = new KlineData(timestamp, open, close, low, high, volume);
                        data.setClosed(isClosed);

                        // 廣播數據到前端 STOMP 頻道
                        messagingTemplate.convertAndSend("/topic/price", data);

                        // 寫入 Redis Stream
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
