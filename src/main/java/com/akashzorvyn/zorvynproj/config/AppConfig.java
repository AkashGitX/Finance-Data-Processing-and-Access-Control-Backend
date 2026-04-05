package com.akashzorvyn.zorvynproj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    public boolean isAiEnabled() {
        return openAiApiKey != null && !openAiApiKey.trim().isEmpty();
    }
}
