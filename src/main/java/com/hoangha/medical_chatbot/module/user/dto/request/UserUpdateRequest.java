package com.hoangha.medical_chatbot.module.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    // Bỏ qua password và username ở đây vì Update Password thường làm thành 1 luồng riêng biệt cho an toàn
}