package com.hoangha.medical_chatbot.module.auth.dto.response;

import com.hoangha.medical_chatbot.module.user.entity.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    UUID id;
    String username;
    String firstName;
    String lastName;
    Role role;

    String token;
    boolean authenticated;
}