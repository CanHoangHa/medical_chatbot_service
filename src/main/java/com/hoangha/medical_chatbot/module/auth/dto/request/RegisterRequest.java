package com.hoangha.medical_chatbot.module.auth.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest{
    String username;
    String password;
    String firstName;
    String lastName;

}
