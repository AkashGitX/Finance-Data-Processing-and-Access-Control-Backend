package com.akashzorvyn.zorvynproj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @Bean
    public ChatClientWrapper chatClientWrapper() {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            log.warn("Spring AI: api-key is empty. Analytics endpoints will return placeholder insights.");
            return new ChatClientWrapper(null);
        }
        log.info("Spring AI: api-key is present. Creating OpenAI ChatClient.");
        try {
            var openAiApi = new org.springframework.ai.openai.api.OpenAiApi(openAiApiKey);
            var options = org.springframework.ai.openai.OpenAiChatOptions.builder()
                    .withModel("gpt-3.5-turbo")
                    .withTemperature(0.7f)
                    .build();
            var client = new org.springframework.ai.openai.OpenAiChatClient(openAiApi, options);
            return new ChatClientWrapper(client);
        } catch (Exception e) {
            log.error("Failed to create Spring AI ChatClient: {}", e.getMessage());
            return new ChatClientWrapper(null);
        }
    }
}
