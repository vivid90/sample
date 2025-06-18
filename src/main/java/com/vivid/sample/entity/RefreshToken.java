package com.vivid.sample.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // 사용자를 식별하는 ID (예: username 또는 User 엔티티의 PK)

    @Column(nullable = false, length = 512) // JWT 토큰 길이를 고려하여 충분한 길이 설정
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    private RefreshToken(String userId, String token, Instant expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public static RefreshToken create(String userId, String token, Instant expiryDate) {
        return new RefreshToken(userId, token, expiryDate);
    }
}
