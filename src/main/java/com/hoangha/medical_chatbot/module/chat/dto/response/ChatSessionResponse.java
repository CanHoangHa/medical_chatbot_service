package com.hoangha.medical_chatbot.module.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ChatSessionResponse {
    private UUID id;
    private String title;
    private LocalDateTime updatedAt;
}