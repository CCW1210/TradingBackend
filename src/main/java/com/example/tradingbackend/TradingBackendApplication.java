package com.example.tradingbackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingBackendApplication {

	public static void main(String[] args) {
		// 載入 .env 文件，並將每個條目設定為系統屬性
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // 如果 .env 文件不存在則忽略
				.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(TradingBackendApplication.class, args);
	}
}
