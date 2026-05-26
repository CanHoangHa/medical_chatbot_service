package com.hoangha.medical_chatbot.module.quota.repository;

import com.hoangha.medical_chatbot.module.quota.entity.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQuotaRepository extends JpaRepository<UserQuota, Long> {
    Optional<UserQuota> findByUserId(UUID userId);
}
