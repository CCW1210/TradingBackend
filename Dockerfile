# 使用 Maven 映像檔進行編譯打包
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# 將 pom.xml 及專案原始碼複製到容器中
COPY pom.xml .
COPY src ./src

# 編譯並打包（可以加上 -DskipTests 跳過測試）
RUN mvn package -DskipTests

# 使用 OpenJDK 映像檔運行應用
FROM openjdk:17-jdk-slim
WORKDIR /app

# 從編譯階段複製產生的 jar 檔到此階段
COPY --from=build /app/target/TradingBackend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
