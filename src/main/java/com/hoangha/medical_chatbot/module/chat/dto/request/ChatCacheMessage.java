package com.hoangha.medical_chatbot.module.chat.dto.request;

import com.hoangha.medical_chatbot.module.chat.entity.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCacheMessage {
    private MessageRole role;
    private String content;
}