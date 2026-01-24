package com.symteo.global.auth;

import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void deleteExpiredUsers() {

    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

    List<User> expiredUsers = userRepository.findByDeletedAtBefore(sevenDaysAgo);

        if (!expiredUsers.isEmpty()) {
        log.info("탈퇴 후 7일 지난 유저 {}명 완전 삭제 시작", expiredUsers.size());

        userRepository.deleteAll(expiredUsers);

        log.info("삭제 완료");
        }
    }
}
