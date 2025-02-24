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

/**
 * KlineSyncService 定時從 Redis Streams 讀取 K 線數據，並批次同步到 PostgreSQL
 * 此版本修正了 read() 方法返回類型與參數的問題，
 * 並採用了顯式轉型來解決泛型不匹配問題，
 * 基於2025年的最新最佳實踐設計。
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
    @Scheduled(fixedRate = 60000) // 每分鐘執行一次
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
                    // 日誌輸出該筆記錄內容，便於確認資料格式
                    log.debug("處理記錄：{}", record.getValue());

                    // 轉換數據
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
