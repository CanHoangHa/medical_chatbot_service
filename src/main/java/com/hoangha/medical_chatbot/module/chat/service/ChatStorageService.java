package com.hoangha.medical_chatbot.module.chat.service;

import com.hoangha.medical_chatbot.module.chat.entity.ChatMessage;
import com.hoangha.medical_chatbot.module.chat.entity.ChatSession;
import com.hoangha.medical_chatbot.module.chat.repository.ChatMessageRepository;
import com.hoangha.medical_chatbot.module.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatStorageService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void saveChatTransaction(ChatSession session, ChatMessage userMsg, ChatMessage aiMsg) {
        chatMessageRepository.saveAll(List.of(userMsg, aiMsg));

        session.setUpdatedAt(java.time.LocalDateTime.now());
        chatSessionRepository.save(session);
    }
}