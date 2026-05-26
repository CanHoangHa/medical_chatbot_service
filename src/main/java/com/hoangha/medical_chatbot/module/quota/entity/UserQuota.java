package com.hoangha.medical_chatbot.module.quota.entity;

import com.hoangha.medical_chatbot.common.BaseEntity;
import com.hoangha.medical_chatbot.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_quotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuota extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "max_tokens", nullable = false)
    private Long maxTokens;

    @Column(name = "used_tokens")
    private Long usedTokens;
}