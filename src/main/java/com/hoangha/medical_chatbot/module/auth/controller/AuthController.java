package com.hoangha.medical_chatbot.module.auth.controller;

import com.hoangha.medical_chatbot.common.ApiResponse;
import com.hoangha.medical_chatbot.module.auth.dto.request.*;
import com.hoangha.medical_chatbot.module.auth.dto.response.AuthenticationResponse;
import com.hoangha.medical_chatbot.module.auth.dto.response.IntrospectResponse;
import com.hoangha.medical_chatbot.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.<String>builder()
                .result("Logout successfully")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) throws Exception {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.refreshToken(request))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authService.introspect(request))
                .build();
    }
}