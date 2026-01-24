package com.symteo.domain.user.repository;

import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 소셜 타입과 소셜 ID로 특정 유저 찾기
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    // 닉네임 중복 검사
    boolean existsByNickname(String nickname);

    List<User> findByDeletedAtBefore(LocalDateTime dateTime);
}
