# TradingBackend

## 專案概述

TradingBackend 是基於 Spring Boot 的中介服務，用於收集、處理、儲存及分發加密貨幣即時交易數據（以 Binance BTC/USDT 1 分鐘 K 線為例），並透過 RESTful API 與 WebSocket 提供歷史與即時更新。

## 主要功能

* **即時收集**：連線 Binance WebSocket (`btcusdt@kline_1m`)
* **流處理**：以 Redis Streams 暫存並推送即時資料
* **持久化**：定期同步至 PostgreSQL，供歷史分析
* **API 提供**：

  * RESTful API：查詢歷史／最新 K 線
  * WebSocket：訂閱即時更新

## 技術

* **語言／框架**：Java 17、Spring Boot 3.4.3
* **資料存取**：Spring Data JPA (PostgreSQL)、Spring Data Redis
* **即時通訊**：Spring WebSocket、Java‑WebSocket
* **其他**：Jackson、Lombok、dotenv‑java
* **容器化**：Docker

## 系統架構與資料流程

1. **數據收集**：

   * `BinanceWebSocketClient` 連線 Binance WebSocket 接收 K 線資料。
2. **流處理與推送**：

   * 將資料寫入 Redis Stream，並由 `/ws` 端點推送即時更新。
3. **歷史持久化**：

   * `KlineSyncService` 定時從 Redis 讀取並存入 PostgreSQL。
4. **資料存取**：

   * **REST API**

     * `GET /api/klines`：歷史 K 線
     * `GET /api/klines/latest`：最新 K 線
   * **WebSocket**

     * 連線 `ws://{host}/ws` 後自動接收即時更新

## 快速開始

### 本地執行

1. 安裝 Java 17、Maven
2. Clone 並建置：

   ```powershell
   git clone https://github.com/CCW1210/TradingBackend.git
   cd TradingBackend
   mvn clean package
   ```
3. 啟動應用：

   ```powershell
   java -jar target/TradingBackend-0.0.1-SNAPSHOT.jar
   ```

### Docker 部署

```powershell
docker build -t trading-backend .
docker run -p 8080:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<POSTGRES_HOST>:5432/<DB_NAME> `
  -e SPRING_DATASOURCE_USERNAME=<USERNAME> `
  -e SPRING_DATASOURCE_PASSWORD=<PASSWORD> `
  -e SPRING_DATA_REDIS_URL=<REDIS_HOST> `
  -e SPRING_DATA_REDIS_PORT=6379 `
  trading-backend
```

## 環境變數

* `SPRING_DATASOURCE_URL`
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `SPRING_DATA_REDIS_URL`
* `SPRING_DATA_REDIS_PORT`

可於開發期間在專案根目錄新增 `.env` 檔，自動載入上述變數。

---
