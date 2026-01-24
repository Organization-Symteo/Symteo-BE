package com.symteo.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_tokens")
public class UserTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼명
    private User user;

    @Column(name = "refresh_token", nullable = false, length = 512)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public UserTokens(User user, String refreshToken, LocalDateTime expiresAt) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    // 토큰 갱신 메서드 (RTR)
    public void updateRefreshToken(String newRefreshToken, LocalDateTime newExpiresAt) {
        this.refreshToken = newRefreshToken;
        this.expiresAt = newExpiresAt;
    }
}
