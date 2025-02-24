// KlineData.java
package com.example.tradingbackend.model;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timestamp;
    private Double open;
    private Double close;
    private Double low;
    private Double high;
    private Double volume;

    // 可選：存放交易訊號資訊
    private String tradeSignal;

    // 自訂建構子用於非持久化操作（例如 WebSocket 轉換用）
    public KlineData(Long timestamp, Double open, Double close, Double low, Double high, Double volume,
            String tradeSignal) {
        this.timestamp = timestamp;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.tradeSignal = tradeSignal;
    }
}
