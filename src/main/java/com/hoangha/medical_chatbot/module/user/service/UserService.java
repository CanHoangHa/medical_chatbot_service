package com.hoangha.medical_chatbot.module.user.service;

import com.hoangha.medical_chatbot.exception.AppException;
import com.hoangha.medical_chatbot.exception.ErrorCode;
import com.hoangha.medical_chatbot.module.user.dto.request.UserCreationRequest;
import com.hoangha.medical_chatbot.module.user.dto.request.UserUpdateRequest;
import com.hoangha.medical_chatbot.module.user.dto.response.UserResponse;
import com.hoangha.medical_chatbot.module.user.entity.User;
import com.hoangha.medical_chatbot.module.user.mapper.UserMapper;
import com.hoangha.medical_chatbot.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ================== LUỒNG CỦA CÁ NHÂN (USER) ==================

    public UserResponse getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateMyProfile(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ================== LUỒNG CỦA ADMIN (QUẢN TRỊ) ==================
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Mã hóa password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Đọc danh sách (có phân trang)
    public Page<UserResponse> getAllUsers(int page, int size) {
        // Trừ 1 vì Spring Data Pageable bắt đầu từ index 0
        Pageable pageable = PageRequest.of(page - 1, size);

        // Repository trả về Page<User>, map() sẽ tự động duyệt và chuyển thành Page<UserResponse>
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    // Đọc chi tiết 1 User
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    // Cập nhật User bất kỳ (Dành cho Admin)
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Xóa User
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Hàm này sẽ tự động kích hoạt UPDATE is_active = false nhờ @SQLDelete ở Entity
        userRepository.delete(user);
    }
}