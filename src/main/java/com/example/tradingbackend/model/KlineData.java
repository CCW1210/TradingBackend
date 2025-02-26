package com.example.tradingbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kline_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineData {

    @Id
    private Long timestamp;
    private Double open;
    private Double close;
    private Double low;
    private Double high;
    private Double volume;
    private Boolean closed; // 表示該根 K 線是否收盤

    // 自訂建構子（不包含 closed 欄位，預設 closed 為 false）
    public KlineData(Long timestamp, Double open, Double close, Double low, Double high, Double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.closed = false;
    }
}
