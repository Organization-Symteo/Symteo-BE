package com.symteo.domain.report.dto;

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
    public static class ReportDetail {
        private Long reportId;
        private String testType;
        private String aiContents;
        private Object scores; // DepressionReports 혹은 AnxietyReports의 점수 데이터
        private LocalDateTime createdAt;
    }
}