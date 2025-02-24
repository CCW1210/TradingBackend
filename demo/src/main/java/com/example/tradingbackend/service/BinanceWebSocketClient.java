// BinanceWebSocketClient.java
package com.example.tradingbackend.service;

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
    private final RedisStreamService redisStreamService; // 後續會用來寫入 Redis Streams

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
                        // 解析 K 線數據，依據 Binance 資料格式做調整
                        // 範例：取得 timestamp, open, close, high, low, volume 與交易訊號（假設有此欄位）
                        long timestamp = jsonNode.path("k").path("t").asLong();
                        double open = jsonNode.path("k").path("o").asDouble();
                        double close = jsonNode.path("k").path("c").asDouble();
                        double high = jsonNode.path("k").path("h").asDouble();
                        double low = jsonNode.path("k").path("l").asDouble();
                        double volume = jsonNode.path("k").path("v").asDouble();
                        boolean isClosed = jsonNode.path("k").path("x").asBoolean();

                        // 根據業務邏輯判斷是否有交易訊號
                        String tradeSignal = null;
                        if (/* 判斷條件 */ false) {
                            tradeSignal = "進場"; // 或 "出場"
                        }

                        // 封裝數據物件
                        KlineData data = new KlineData(timestamp, open, close, low, high, volume, tradeSignal);

                        // 廣播給前端（/topic/price）
                        messagingTemplate.convertAndSend("/topic/price", data);

                        // 寫入 Redis Streams
                        redisStreamService.addKlineData(data);

                    } catch (Exception e) {
                        log.error("解析 Binance 數據失敗", e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.warn("Binance WebSocket closed: {} - {}", code, reason);
                    reconnect();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Binance WebSocket error: ", ex);
                    reconnect();
                }
            };

            client.connect();
        } catch (Exception e) {
            log.error("建立 Binance WebSocket 連線失敗", e);
            reconnect();
        }
    }

    // 重連機制
    private void reconnect() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("重新連線 Binance WebSocket...");
        connect();
    }
}
