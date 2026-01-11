package com.symteo.domain.auth.repository;

import com.symteo.domain.user.entity.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserTokens, Long> {
    Optional<UserTokens> findByRefreshToken(String refreshToken);
    // 특정 유저의 토큰 삭제 (로그아웃 등)
    void deleteByUser_Id(Long userId);
}
