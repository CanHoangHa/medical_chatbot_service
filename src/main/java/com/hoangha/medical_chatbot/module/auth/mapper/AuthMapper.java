package com.hoangha.medical_chatbot.module.auth.mapper;

import com.hoangha.medical_chatbot.module.auth.dto.response.AuthenticationResponse;
import com.hoangha.medical_chatbot.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "authenticated", source = "isAuthenticated")
    AuthenticationResponse toAuthResponse(User user, String token, boolean isAuthenticated);
}
