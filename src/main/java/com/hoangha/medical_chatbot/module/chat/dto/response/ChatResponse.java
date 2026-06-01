package com.hoangha.medical_chatbot.module.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ChatResponse {
    private UUID sessionId; // Trả về để Frontend biết đang chat ở Session nào
    private String reply;
}
