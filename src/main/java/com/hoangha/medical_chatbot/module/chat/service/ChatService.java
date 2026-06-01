package com.hoangha.medical_chatbot.module.chat.service;

import com.hoangha.medical_chatbot.exception.AppException;
import com.hoangha.medical_chatbot.exception.ErrorCode;
import com.hoangha.medical_chatbot.infrastructure.redis.RedisChatCacheService;
import com.hoangha.medical_chatbot.module.chat.dto.request.ChatCacheMessage;
import com.hoangha.medical_chatbot.module.chat.dto.request.ChatRequest;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatMessageResponse;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatResponse;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatSessionResponse;
import com.hoangha.medical_chatbot.module.chat.entity.ChatMessage;
import com.hoangha.medical_chatbot.module.chat.entity.ChatSession;
import com.hoangha.medical_chatbot.module.chat.entity.MessageRole;
import com.hoangha.medical_chatbot.module.chat.repository.ChatMessageRepository;
import com.hoangha.medical_chatbot.module.chat.repository.ChatSessionRepository;
import com.hoangha.medical_chatbot.module.user.entity.User;
import com.hoangha.medical_chatbot.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;

    private final RedisChatCacheService redisChatCacheService;
    private final ChatStorageService chatStorageService;

    public ChatResponse processChat(ChatRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        ChatSession session;
        if (request.getSessionId() == null) {
            ChatSession newSession = ChatSession.builder()
                    .user(user)
                    .title("Tư vấn y tế mới") // Sẽ làm auto-title sau
                    .build();

            session = chatSessionRepository.save(newSession);
        } else {
            session = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new AppException(ErrorCode.CHAT_SESSION_NOT_FOUND));

            if (!session.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        List<ChatCacheMessage> cachedHistory = new ArrayList<>();
        if (request.getSessionId() != null) {
            cachedHistory = redisChatCacheService.getContextHistory(session.getId());


            if (cachedHistory.isEmpty()) {
                log.warn("Cache Miss cho session {}. Đang phục hồi từ DB...", session.getId());

                Pageable top10 = PageRequest.of(0, 10);
                List<ChatMessage> dbMessages = chatMessageRepository.findLatestMessages(session.getId(), top10);
                Collections.reverse(dbMessages); // Reverse so that oldest messages come first

                cachedHistory = dbMessages.stream()
                        .map(msg -> new ChatCacheMessage(msg.getRole(), msg.getContent()))
                        .toList();

                redisChatCacheService.hydrateCache(session.getId(), cachedHistory);
            }
        }


        List<Message> springAiMessages = new ArrayList<>();
        for (ChatCacheMessage msg : cachedHistory) {
            if (msg.getRole() == MessageRole.USER) {
                springAiMessages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                springAiMessages.add(new AssistantMessage(msg.getContent()));
            }
        }

        springAiMessages.add(new UserMessage(request.getMessage()));

        log.info("Chuẩn bị gọi AI. Kích thước Context: {} tin nhắn.", springAiMessages.size());


        String aiReplyContent;
        try {
            aiReplyContent = chatClient.prompt()
                    .messages(springAiMessages)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Lỗi khi gọi LLM: ", e);
            throw new AppException(ErrorCode.LLM_SERVICE_ERROR);
        }


        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(request.getMessage())
                .build();

        ChatMessage aiMsg = ChatMessage.builder()
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content(aiReplyContent)
                .build();

        chatStorageService.saveChatTransaction(session, userMsg, aiMsg);

        redisChatCacheService.addMessage(session.getId(), new ChatCacheMessage(MessageRole.USER, request.getMessage()));
        redisChatCacheService.addMessage(session.getId(), new ChatCacheMessage(MessageRole.ASSISTANT, aiReplyContent));


        return ChatResponse.builder()
                .sessionId(session.getId())
                .reply(aiReplyContent)
                .build();
    }



    public List<ChatSessionResponse> getMySessions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        return sessions.stream()
                .map(session -> ChatSessionResponse.builder()
                        .id(session.getId())
                        .title(session.getTitle())
                        .updatedAt(session.getUpdatedAt())
                        .build())
                .toList();
    }

    public List<ChatMessageResponse> getSessionMessages(UUID sessionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        return messages.stream()
                .map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .toList();
    }

    public void deleteSession(UUID sessionId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Nhờ @SQLDelete ở Entity, lệnh này sẽ tự biến thành UPDATE is_active = false
        chatSessionRepository.delete(session);
    }
}
