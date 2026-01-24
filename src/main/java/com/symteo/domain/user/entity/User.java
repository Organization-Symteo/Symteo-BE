package com.symteo.domain.user.entity;

import com.symteo.domain.user.enums.Role;
import com.symteo.domain.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id") // ERD의 PK 컬럼명 반영
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname", length = 10)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 연관 관계 매핑
    // 회원 탈퇴 기능을 위해 자식 엔티티들과의 관계에 cascade = CascadeType.ALL과 orphanRemoval = true를 넣어주세요
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTokens> userTokens = new ArrayList<>();

    @Builder
    public User(SocialType socialType, String socialId, com.symteo.domain.user.enums.Role role) {
        this.socialType = socialType;
        this.socialId = socialId;
        this.role = role;
    }

    // 닉네임 설정 메서드 (회원가입 완료 처리)
    public void authorizeUser(String nickname) {
        this.nickname = nickname;
        this.role = Role.USER; // 정회원 승격
    }

    // 탈퇴 처리 메서드 (Soft Delete)
    public void deleteSoftly() {
        this.deletedAt = LocalDateTime.now();
    }
}
