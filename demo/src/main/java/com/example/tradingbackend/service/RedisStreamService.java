// RedisStreamService.java
package com.example.tradingbackend.service;

import com.example.tradingbackend.model.KlineData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RedisStreamService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STREAM_KEY = "kline_stream:BTCUSDT:1m";

    public RedisStreamService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addKlineData(KlineData data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("timestamp", data.getTimestamp());
            message.put("open", data.getOpen());
            message.put("close", data.getClose());
            message.put("high", data.getHigh());
            message.put("low", data.getLow());
            message.put("volume", data.getVolume());
            message.put("tradeSignal", data.getTradeSignal());

            redisTemplate.opsForStream().add(STREAM_KEY, message);
            log.info("已寫入 Redis Stream：{}", message);
        } catch (Exception e) {
            log.error("寫入 Redis Stream 失敗", e);
        }
    }
}
