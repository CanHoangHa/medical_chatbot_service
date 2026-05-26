package com.hoangha.medical_chatbot.module.user.dto.request;

import com.hoangha.medical_chatbot.module.user.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreationRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
}