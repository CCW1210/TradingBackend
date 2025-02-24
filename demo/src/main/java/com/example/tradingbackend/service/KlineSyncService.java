// KlineSyncService.java
package com.example.tradingbackend.service;

import com.example.tradingbackend.model.KlineData;
import com.example.tradingbackend.repository.KlineDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KlineSyncService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KlineDataRepository repository;
    private static final String STREAM_KEY = "kline_stream:BTCUSDT:1m";

    public KlineSyncService(RedisTemplate<String, Object> redisTemplate, KlineDataRepository repository) {
        this.redisTemplate = redisTemplate;
        this.repository = repository;
    }

    @Scheduled(fixedRate = 60000) // 每分鐘執行一次
    public void syncKlineData() {
        // 根據需求設定讀取數量與範圍
        StreamReadOptions options = StreamReadOptions.empty().count(100);
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .read(StreamOffset.fromStart(STREAM_KEY), options, MapRecord.class);
        if (records != null && !records.isEmpty()) {
            for (MapRecord<String, Object, Object> record : records) {
                try {
                    // 解析 record 中的數據並轉換為 KlineData 物件
                    Long timestamp = Long.parseLong(record.getValue().get("timestamp").toString());
                    Double open = Double.valueOf(record.getValue().get("open").toString());
                    Double close = Double.valueOf(record.getValue().get("close").toString());
                    Double low = Double.valueOf(record.getValue().get("low").toString());
                    Double high = Double.valueOf(record.getValue().get("high").toString());
                    Double volume = Double.valueOf(record.getValue().get("volume").toString());
                    String tradeSignal = record.getValue().get("tradeSignal") != null
                            ? record.getValue().get("tradeSignal").toString()
                            : null;

                    KlineData data = new KlineData(timestamp, open, close, low, high, volume, tradeSignal);
                    repository.save(data);
                    // 可考慮從 Redis 刪除已同步的記錄
                } catch (Exception e) {
                    log.error("同步 K 線數據失敗", e);
                }
            }
        }
    }
}
