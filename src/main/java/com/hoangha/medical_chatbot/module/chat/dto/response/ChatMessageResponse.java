package com.hoangha.medical_chatbot.module.chat.dto.response;

import com.hoangha.medical_chatbot.module.chat.entity.MessageRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageResponse {
    private Long id;
    private MessageRole role; // Để UI biết in ra bên trái (ASSISTANT) hay bên phải (USER)
    private String content;
    private LocalDateTime createdAt;
}