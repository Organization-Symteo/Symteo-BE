package com.symteo.domain.home.controller;

import com.symteo.domain.home.dto.HomeResponseDto;
import com.symteo.domain.home.service.HomeService;
import com.symteo.domain.home.service.TodayEmotionWeatherService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final TodayEmotionWeatherService todayEmotionWeatherService;

    // 홈 화면 전체 데이터 조회
    @GetMapping("/")
    public ApiResponse<HomeResponseDto> getHomeDashboard(
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess(homeService.getHomeDashboard(userId));
    }

    // 오늘의 감정 날씨 수정 및 생성
    @PatchMapping("/today-weather")
    public ApiResponse<Integer> updateWeather(
            @AuthenticationPrincipal Long userId,
            @RequestParam Integer weather) {
        return ApiResponse.onSuccess(todayEmotionWeatherService.updateOrCreateEmotion(userId, weather));
    }
}