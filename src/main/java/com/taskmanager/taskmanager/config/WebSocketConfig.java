package com.taskmanager.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // ✅ Using specific origins instead of "*" to prevent handshake conflicts
                .setAllowedOriginPatterns("http://127.0.0.1:5500", "http://localhost:5500")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // ✅ prefix for server -> client messages (e.g., /topic/tasks)
        registry.enableSimpleBroker("/topic");

        // ✅ prefix for client -> server messages (e.g., /app/updateTask)
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Note: Since you are using JWT, you may eventually need to add
    // configureClientInboundChannel here to intercept the CONNECT frame
    // and validate the token.
}
