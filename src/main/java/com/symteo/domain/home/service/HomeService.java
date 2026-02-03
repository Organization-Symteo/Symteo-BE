package com.symteo.domain.home.service;

import com.symteo.domain.home.dto.HomeResponseDto;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final UserRepository userRepository;
    private final TodayLineService todayLineService;
    private final TodayEmotionWeatherService todayEmotionWeatherService;

    @Transactional(readOnly = true)
    public HomeResponseDto getHomeDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 오늘의 한 줄
        String line = todayLineService.getTodayLine(userId);

        // 오늘의 감정 날씨
        Integer weather = todayEmotionWeatherService.getTodayEmotion(userId);

        return new HomeResponseDto(line, weather, user.getNickname());
    }
}
