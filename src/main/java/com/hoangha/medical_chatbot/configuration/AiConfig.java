package com.hoangha.medical_chatbot.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient medicalChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("Bạn là một chuyên gia y tế ảo và bác sĩ tư vấn. Hãy trả lời các câu hỏi về sức khỏe một cách chuyên nghiệp, an toàn, có tính an ủi và luôn khuyên người bệnh đi khám thực tế nếu bệnh nặng.")
                .build();
    }
}