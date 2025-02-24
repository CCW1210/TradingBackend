package com.example.tradingbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 設定簡單內建 broker，廣播到 /topic 路徑
        config.enableSimpleBroker("/topic");
        // 客戶端發送時的前綴
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 客戶端連線的 endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 生產環境建議限制來源
                .withSockJS();
    }
}
