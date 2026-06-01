package com.hoangha.medical_chatbot.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // System Errors
    UNCATEGORIZED_EXCEPTION("Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ENUM_VALUE("Invalid enum value", HttpStatus.BAD_REQUEST),

    // Auth Errors
    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("You do not have permission", HttpStatus.FORBIDDEN),
    USER_NOT_EXISTED("User does not exist", HttpStatus.NOT_FOUND),
    USER_DISABLED("User account is disabled", HttpStatus.FORBIDDEN),
    USERNAME_EXISTED("Username already exists", HttpStatus.BAD_REQUEST),

    // Chat & AI Errors
    CHAT_SESSION_NOT_FOUND("Chat session not found", HttpStatus.NOT_FOUND),
    RATE_LIMIT_EXCEEDED("Too many requests. Please slow down.", HttpStatus.TOO_MANY_REQUESTS),
    OUT_OF_QUOTA("You have exceeded your token quota.", HttpStatus.PAYMENT_REQUIRED),
    LLM_SERVICE_ERROR("AI Service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // Medical Data Errors
    PATIENT_RECORD_NOT_FOUND("Patient medical record not found", HttpStatus.NOT_FOUND),

    //REDIS CACHE ERRORS


    //OBJECT MAPPER ERROR
    DESERIALIZATION_ERROR("Error deserializing object", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    final String message;
    final HttpStatus statusCode;

    ErrorCode(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}