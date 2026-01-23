package com.symteo.domain.user.dto;

import lombok.Builder;

@Builder
public record UserSettingsResponse(
    // 환경 설정 - 알림 설정
    Boolean isCheerMsgOn, // 응원 메세지
    Boolean isAnalysisReadyOn, // 검사지 분석 완료
    Boolean isMonthlyReportOn, // 월간 진단 알림

    // 사용자 설정 - 잠금 설정
    Boolean isLockEnabled, // 잠금 설정

    // 앱 정보
    String appVersion // 앱 버전
) {
    public static UserSettingsResponse of(
        Boolean isCheerMsgOn,
        Boolean isAnalysisReadyOn,
        Boolean isMonthlyReportOn,
        Boolean isLockEnabled,
        String appVersion
    ) {
        return UserSettingsResponse.builder()
                .isCheerMsgOn(isCheerMsgOn)
                .isAnalysisReadyOn(isAnalysisReadyOn)
                .isMonthlyReportOn(isMonthlyReportOn)
                .isLockEnabled(isLockEnabled)
                .appVersion(appVersion)
                .build();
    }
}

