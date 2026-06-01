package com.hoangha.medical_chatbot.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoangha.medical_chatbot.exception.AppException;
import com.hoangha.medical_chatbot.exception.ErrorCode;
import com.hoangha.medical_chatbot.module.chat.dto.request.ChatCacheMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatCacheService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    private static final int MAX_HISTORY_SIZE = 10;
    private static final String KEY_PREFIX = "chat_history:";

    public void addMessage(UUID sessionId, ChatCacheMessage message){
        String key = KEY_PREFIX + sessionId.toString();
        try{
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key,jsonMessage);

            redisTemplate.opsForList().trim(key, -MAX_HISTORY_SIZE, -1);

            redisTemplate.expire(key, 1, TimeUnit.HOURS);

        }
        catch (Exception e) {
            log.error("Error adding message to Redis cache for session {}: {}", sessionId, e.getMessage());
        }
    }

    public List<ChatCacheMessage> getContextHistory(UUID sessionId){
        String key = KEY_PREFIX + sessionId.toString();

        List<String> jsonMessages = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonMessages == null || jsonMessages.isEmpty()) {
            return new ArrayList<>();
        }
        return jsonMessages.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatCacheMessage.class);
                    } catch (Exception e) {
                        redisTemplate.delete(key);
                        log.error("Error deserializing message from Redis cache for session {}: {}", sessionId, e.getMessage());
                        throw new AppException(ErrorCode.DESERIALIZATION_ERROR);
                    }
                }).toList();
    }

    public void hydrateCache(UUID sessionId, List<ChatCacheMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            return;
        }

        String key = KEY_PREFIX + sessionId;

        try {
            redisTemplate.delete(key);
            for (ChatCacheMessage msg : messages) {
                String json = objectMapper.writeValueAsString(msg);
                redisTemplate.opsForList().rightPush(key, json);
            }
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn(
                    "Failed to hydrate redis cache for session {}",
                    sessionId,
                    e
            );
        }
    }
}
