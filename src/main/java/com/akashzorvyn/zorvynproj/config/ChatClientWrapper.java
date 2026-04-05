package com.akashzorvyn.zorvynproj.config;

import lombok.Getter;
import org.springframework.ai.chat.ChatClient;


@Getter
public class ChatClientWrapper {

    private final ChatClient chatClient;

    public ChatClientWrapper(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public boolean isAvailable() {
        return chatClient != null;
    }

    public String call(String prompt) {
        if (!isAvailable()) {
            return "AI analysis is not available — Spring AI API key is not configured. " +
                   "Please set spring.ai.openai.api-key in application.properties to enable AI-powered insights. " +
                   "The data above reflects real aggregated financial records from the database.";
        }
        return chatClient.call(prompt);
    }
}
