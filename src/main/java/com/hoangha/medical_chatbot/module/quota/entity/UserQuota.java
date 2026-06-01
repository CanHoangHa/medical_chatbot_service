package com.hoangha.medical_chatbot.module.quota.entity;

import com.hoangha.medical_chatbot.common.BaseEntity;
import com.hoangha.medical_chatbot.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_quotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE user_quotas SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true")
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