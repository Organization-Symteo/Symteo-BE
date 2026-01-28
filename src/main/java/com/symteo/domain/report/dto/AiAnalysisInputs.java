package com.symteo.domain.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// AI 분석에 필요한 입력 데이터 묶음
@Getter
@Builder
public class AiAnalysisInputs {
    private String userName;
    private int totalScore;
    private String severityLabel;
    private List<String> highScoresSymptoms; // 2점 이상 체크된 문항들
    private String userSymptomsText;        // 실제 사용자 답변 내용 요약
    private boolean q9Flag;                 // 자살 사고 여부
}