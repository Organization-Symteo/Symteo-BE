package com.symteo.domain.report.constant;

public class ReportsConstant {
    // 점수 임계치 (Magic Numbers)
    public static final double ATTACHMENT_THRESHOLD = 3.0;
    public static final int PHQ_HIGH_RISK = 20;
    public static final int PHQ_MANAGEMENT = 15;
    public static final int STRESS_VERY_DANGEROUS = 19;

    // UI 색상 코드
    public static final String COLOR_DANGER = "#F4574F";
    public static final String COLOR_WARNING = "#FFAC79";
    public static final String COLOR_CAUTION = "#FFE8A9";
    public static final String COLOR_SAFE = "#63B19B";

    // AI 구분자
    public static final String AI_CONTENT_DELIMITER = "\\|\\|";
}