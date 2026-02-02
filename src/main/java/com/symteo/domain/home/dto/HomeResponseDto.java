package com.symteo.domain.home.dto;

public record HomeResponseDto(
        String todayLine,       // 오늘의 한 줄
        Integer todayWeather,   // 오늘의 감정 날씨 (미선택 시 null)
        String nickname        // 사용자 닉네임
) {}