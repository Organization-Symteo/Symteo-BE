package com.symteo.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReportsResponse {

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class CreateReportResult {
        private Long reportId;
        private String testType;
        private LocalDateTime createdAt;
    }

    /**
     * 우울/불안 통합 리포트 상세 (Complex)
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class DepressionAnxietyReportDetail {
        private Long reportId;
        private String testType;
        private OverallSummary summary;         // 종합 결과
        private PhqDetail phq9;                // 우울 섹션
        private GadDetail gad7;                // 불안 섹션
        private List<AiInsightCard> aiInsightCards; // AI 정밀 분석 카드
        private String depressionAiContent;    // 우울 AI 본문
        private String anxietyAiContent;       // 불안 AI 본문
        private boolean emergencyFlag;         // 긴급 도움 섹션 노출 여부
        private LocalDateTime createdAt;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class OverallSummary {
        private double averageScore;
        private String statusLabel;
        private String statusColor;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class PhqDetail {
        private int totalScore;
        private double needleDeg;              // 게이지 바늘 회전 각도
        private List<ClusterResult> clusters;  // 증상 클러스터 바 리스트
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class GadDetail {
        private int totalScore;
        private double needleDeg;
        private List<ClusterResult> clusters;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class ClusterResult {
        private String name;
        private double scoreRatio;             // 0.0~1.0 (바 길이)
        private String color;                  // 단계별 색상 코드
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class AiInsightCard {
        private String id;
        private String title;
    }

    /**
     * 스트레스/번아웃 리포트 상세 (기존 유지)
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IntegratedReportDetail {
        private Long reportId;
        private String testType;
        private int batteryPercent;
        private String batteryColor;
        private String batteryGuide;
        private StressDetail stress;
        private BurnoutDetail burnout;
        private List<String> aiInsights;
        private String aiFullContent;
        private LocalDateTime createdAt;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class StressDetail {
        private int pssScore;
        private String stressLevel;
        private String controlLevel;
        private String overloadLevel;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class BurnoutDetail {
        private String exhaustionLevel;
        private String cynicismLevel;
        private String inefficacyLevel;
        private String totalLevel;
    }
}