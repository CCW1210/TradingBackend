package com.example.tradingbackend.service;

import com.example.tradingbackend.model.KlineData;
import com.example.tradingbackend.repository.KlineDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * KlineSyncService 定時從 Redis Streams 讀取 K 線數據，並批次同步到 PostgreSQL。
 * 已移除所有與 tradeSignal 相關的處理邏輯。
 */
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

    @SuppressWarnings("unchecked")
    @Scheduled(fixedRate = 30000) // 每分鐘執行一次
    public void syncKlineData() {
        log.info("開始執行同步任務...");
        // 設定讀取選項：最多讀取100筆記錄
        StreamReadOptions options = StreamReadOptions.empty().count(100);

        List<MapRecord<String, Object, Object>> records = (List<MapRecord<String, Object, Object>>) (List<?>) redisTemplate
                .opsForStream()
                .read(MapRecord.class, options, StreamOffset.fromStart(STREAM_KEY));

        if (records != null && !records.isEmpty()) {
            log.info("讀取到 {} 筆記錄", records.size());
            for (MapRecord<String, Object, Object> record : records) {
                try {
                    Map<Object, Object> value = record.getValue();
                    // 輸出整個記錄以便檢查數據完整性
                    log.debug("處理記錄：{}", value);

                    // 檢查必須欄位是否存在
                    if (value.get("timestamp") == null ||
                            value.get("open") == null ||
                            value.get("close") == null ||
                            value.get("low") == null ||
                            value.get("high") == null ||
                            value.get("volume") == null) {
                        log.warn("記錄缺少必要欄位，跳過處理：{}", value);
                        continue;
                    }

                    Long timestamp = Long.parseLong(value.get("timestamp").toString());
                    Double open = Double.valueOf(value.get("open").toString());
                    Double close = Double.valueOf(value.get("close").toString());
                    Double low = Double.valueOf(value.get("low").toString());
                    Double high = Double.valueOf(value.get("high").toString());
                    Double volume = Double.valueOf(value.get("volume").toString());

                    KlineData data = new KlineData(timestamp, open, close, low, high, volume);
                    repository.save(data);
                    log.info("成功同步記錄至 PostgreSQL：{}", data);
                } catch (Exception e) {
                    log.error("同步 Kline 數據失敗", e);
                }
            }
        } else {
            log.info("沒有讀取到任何記錄");
        }
    }
}
