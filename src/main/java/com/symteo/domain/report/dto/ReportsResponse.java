package com.symteo.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.symteo.domain.diagnose.enums.DiagnoseType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReportsResponse {

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class CreateReportResult {
        private Long reportId;
        private DiagnoseType testType;
        private LocalDateTime createdAt;
    }

    // 우울/불안 리포트 상세
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class DepressionAnxietyReportDetail {
        private Long reportId;
        private DiagnoseType testType;
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

    // 스트레스/번아웃 리포트 상세
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IntegratedReportDetail {
        private Long reportId;
        private DiagnoseType testType;
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

    // 성향 리포트
    @Builder @Getter @NoArgsConstructor @AllArgsConstructor
    public static class AttachmentReportDetail {
        private Long reportId;
        private String userName;
        private String attachmentType;

        // 관계 지도 및 바 차트용 데이터
        private AttachmentScore anxiety;
        private AttachmentScore avoidance;

        // 애착 분석 카드 데이터 (각 2개씩)
        private List<AttachmentCard> stressPoints;
        private List<AttachmentCard> strengthPoints;

        // AI가 작성한 최종 리포트 본문 (3문단)
        private String aiFullContent;
        private String actionGuideSentence;

        private LocalDateTime createdAt;
    }

    @Builder @Getter @NoArgsConstructor @AllArgsConstructor
    public static class AttachmentScore {
        private double score;
        private int percentage;
        private String stateLabel;
        private String color;
        private String stateComment;
    }

    @Builder @Getter @NoArgsConstructor @AllArgsConstructor
    public static class AttachmentCard {
        private String title;
        private String description;
    }
}