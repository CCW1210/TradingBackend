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
    private Long timestamp;
    private Double open;
    private Double close;
    private Double low;
    private Double high;
    private Double volume;
}
