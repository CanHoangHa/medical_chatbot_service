package com.hoangha.medical_chatbot.module.chat.repository;

import com.hoangha.medical_chatbot.module.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    @Query("SELECT c FROM ChatMessage c WHERE c.session.id = :sessionId ORDER BY c.createdAt DESC")
    List<ChatMessage> findLatestMessages(@Param("sessionId") UUID sessionId, Pageable pageable);
}