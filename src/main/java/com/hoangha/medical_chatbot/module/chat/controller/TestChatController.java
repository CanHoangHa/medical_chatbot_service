package com.hoangha.medical_chatbot.module.chat.controller;

import com.hoangha.medical_chatbot.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class TestChatController {

    private final ChatClient chatClient;

    // Inject ChatClient.Builder do Spring AI tự động cấu hình sẵn
    public TestChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/test")
    public ApiResponse<String> testGemini(@RequestParam String message) {
        // Gọi lên Gemini và lấy content trả về
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        return ApiResponse.<String>builder()
                .result(response)
                .build();
    }
}