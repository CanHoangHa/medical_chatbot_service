package com.hoangha.medical_chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MedicalChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalChatbotApplication.class, args);
    }

}
