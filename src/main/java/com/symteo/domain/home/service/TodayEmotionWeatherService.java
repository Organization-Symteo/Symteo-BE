package com.symteo.domain.home.service;

import com.symteo.domain.home.entity.TodayEmotions;
import com.symteo.domain.home.repository.TodayEmotionWeatherRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TodayEmotionWeatherService {
    private final TodayEmotionWeatherRepository todayEmotionWeatherRepository;
    private final UserRepository userRepository;

    //  오늘의 감정 날씨 생성 및 수정 api
    public Integer updateOrCreateEmotion(Long userId, Integer emotionValue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        LocalDate today = LocalDate.now();

        Optional<TodayEmotions> existingEmotion = todayEmotionWeatherRepository
                .findByUserAndCreatedAtBetween(user, today.atStartOfDay(), today.atTime(LocalTime.MAX));

        if (existingEmotion.isPresent()) {
            // 수정 로직
            TodayEmotions te = existingEmotion.get();
            te.updateEmotion(emotionValue);
            return te.getEmotion();
        } else {
            // 생성 로직
            TodayEmotions newEmotion = TodayEmotions.builder()
                    .user(user)
                    .emotion(emotionValue)
                    .build();

            return todayEmotionWeatherRepository.save(newEmotion).getEmotion();
        }
    }

    // 오늘 감정 날씨 조회 api
    @Transactional(readOnly = true)
    public Integer getTodayEmotion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        LocalDate today = LocalDate.now();

        // 오늘 날짜의 감정 기록 조회
        return todayEmotionWeatherRepository
                .findByUserAndCreatedAtBetween(user, today.atStartOfDay(), today.atTime(LocalTime.MAX))
                .map(TodayEmotions::getEmotion)
                .orElse(null); // 아직 선택 안 했으면 null 반환
    }
}