package com.symteo.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReportDetail {
        private Long reportId;
        private String testType;

        // AI 분석글을 도메인별로 분리
        private String depressionAiContent;
        private String anxietyAiContent;

        private Object depressionScores;
        private Object anxietyScores;
        private LocalDateTime createdAt;
    }
}