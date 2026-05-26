package com.hoangha.medical_chatbot.module.user.mapper;

import com.hoangha.medical_chatbot.module.user.dto.response.UserResponse;
import com.hoangha.medical_chatbot.module.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}