# 使用 OpenJDK 17 的輕量映像檔
FROM openjdk:17-jdk-slim

# 設定工作目錄
WORKDIR /app

# 將 Maven 編譯後的 JAR 檔複製到容器中
COPY target/TradingBackend-0.0.1-SNAPSHOT.jar app.jar

# 暴露容器內部的 8080 埠
EXPOSE 8080

# 設定容器啟動時執行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]
