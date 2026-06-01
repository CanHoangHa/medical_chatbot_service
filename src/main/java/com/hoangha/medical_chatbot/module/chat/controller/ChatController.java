package com.hoangha.medical_chatbot.module.chat.controller;

import com.hoangha.medical_chatbot.common.ApiResponse;
import com.hoangha.medical_chatbot.module.chat.dto.request.ChatRequest;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatMessageResponse;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatResponse;
import com.hoangha.medical_chatbot.module.chat.dto.response.ChatSessionResponse;
import com.hoangha.medical_chatbot.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/message")
    public ApiResponse<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        return ApiResponse.<ChatResponse>builder()
                .result(chatService.processChat(request))
                .build();
    }

    // Lấy danh sách Sidebar
    @GetMapping
    public ApiResponse<List<ChatSessionResponse>> getMySessions() {
        return ApiResponse.<List<ChatSessionResponse>>builder()
                .result(chatService.getMySessions())
                .build();
    }

    // Lấy lịch sử 1 đoạn chat
    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getSessionMessages(@PathVariable UUID sessionId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.getSessionMessages(sessionId))
                .build();
    }

    // Xóa đoạn chat
    @DeleteMapping("/{sessionId}")
    public ApiResponse<String> deleteSession(@PathVariable UUID sessionId) {
        chatService.deleteSession(sessionId);
        return ApiResponse.<String>builder()
                .result("Chat session deleted successfully")
                .build();
    }
}
