package com.symteo.domain.home.repository;

import com.symteo.domain.home.entity.TodayEmotions;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodayEmotionWeatherRepository extends JpaRepository<TodayEmotions, Long> {
    Optional<TodayEmotions> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
