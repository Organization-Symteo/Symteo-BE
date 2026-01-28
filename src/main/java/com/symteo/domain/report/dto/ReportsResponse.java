package com.symteo.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReportsResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReportResult {
        private Long reportId;
        private String testType;
        private LocalDateTime createdAt;
    }

    // 우울/불안 리포트 상세
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DepressionAnxietyReportDetail {
        private Long reportId;
        private String testType;
        private String depressionAiContent;
        private String anxietyAiContent;
        private Object depressionScores;
        private Object anxietyScores;
        private LocalDateTime createdAt;
    }

    // 스트레스/번아웃 리포트 상세
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IntegratedReportDetail {
        private Long reportId;
        private String testType;

        // 마음배터리 정보
        private int batteryPercent;
        private String batteryColor;
        private String batteryGuide;

        // 스트레스(PSS) 상세 수치
        private StressDetail stress;

        // 번아웃 상세 수치
        private BurnoutDetail burnout;

        // AI 정밀 통찰 (상단 박스용 최대 3개)
        private List<String> aiInsights;

        // AI 분석 리포트 본문 (3문단)
        private String aiFullContent;

        private LocalDateTime createdAt;
    }

    // 스트레스/번아웃 그래프용 상세 데이터 구조
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressDetail {
        private int pssScore;
        private String stressLevel;     // 정상, 약간, 중간, 심한
        private String controlLevel;    // 매우 낮음 ~ 매우 높음
        private String overloadLevel;   // 매우 낮음 ~ 매우 높음
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BurnoutDetail {
        private String exhaustionLevel; // 정서적 소진
        private String cynicismLevel;   // 비인격화
        private String inefficacyLevel; // 성취감 저하
        private String totalLevel;      // 종합 등급
    }
}