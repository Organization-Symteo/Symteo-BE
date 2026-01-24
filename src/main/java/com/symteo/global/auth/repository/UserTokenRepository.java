package com.symteo.global.auth.repository;

import com.symteo.domain.user.entity.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserTokens, Long> {
    Optional<UserTokens> findByRefreshToken(String refreshToken);

    void deleteAllByUserId(Long userId);
}