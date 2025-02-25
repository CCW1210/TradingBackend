package com.example.tradingbackend.service;

import com.example.tradingbackend.model.KlineData;
import com.example.tradingbackend.repository.KlineDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 使用 range() 方法直接讀取原始記錄（不進行自動映射）
    @Scheduled(fixedRate = 60000) // 每分鐘執行一次
    public void syncKlineData() {
        log.info("開始執行同步任務...");
        try {
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                    .range(STREAM_KEY, Range.unbounded());

            if (records == null || records.isEmpty()) {
                log.info("沒有讀取到任何記錄");
                return;
            }

            log.info("讀取到 {} 筆記錄", records.size());
            for (MapRecord<String, Object, Object> record : records) {
                try {
                    // 將原始 hash 轉換成 Map<String, String>
                    Map<Object, Object> rawHash = record.getValue();
                    Map<String, String> hash = new HashMap<>();
                    rawHash.forEach((key, value) -> {
                        hash.put(String.valueOf(key), value == null ? null : String.valueOf(value));
                    });

                    log.debug("處理記錄：{}", hash);

                    // 檢查必要欄位是否存在且不為空（包含 closed 欄位）
                    if (hash.get("timestamp") == null ||
                            hash.get("open") == null ||
                            hash.get("close") == null ||
                            hash.get("low") == null ||
                            hash.get("high") == null ||
                            hash.get("volume") == null ||
                            hash.get("closed") == null) {
                        log.warn("記錄缺少必要欄位，跳過處理：{}", hash);
                        continue;
                    }

                    Long timestamp = Long.parseLong(hash.get("timestamp"));
                    Double open = Double.valueOf(hash.get("open"));
                    Double close = Double.valueOf(hash.get("close"));
                    Double low = Double.valueOf(hash.get("low"));
                    Double high = Double.valueOf(hash.get("high"));
                    Double volume = Double.valueOf(hash.get("volume"));
                    boolean isClosed = Boolean.parseBoolean(hash.get("closed"));

                    KlineData data = new KlineData(timestamp, open, close, low, high, volume);
                    data.setClosed(isClosed);

                    repository.save(data);
                    log.info("成功同步記錄至 PostgreSQL：{}", data);
                } catch (Exception e) {
                    log.error("轉換或寫入記錄失敗：{}", record.getValue(), e);
                }
            }
        } catch (Exception e) {
            log.error("讀取 Redis Stream 時出現錯誤：", e);
        }
    }
}
