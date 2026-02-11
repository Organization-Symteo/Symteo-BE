package com.symteo.domain.counsel.service;

import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.service.AttachmentReportsService;
import com.symteo.domain.report.service.DepressionAnxietyReportsService;
import com.symteo.domain.report.service.StressReportsService;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptFactoryImpl implements PromptFactory{
    private final UserRepository userRepository;

    private final AttachmentReportsService attachmentReportsService;
    private final StressReportsService stressReportsService;
    private final DepressionAnxietyReportsService depressionAnxietyReportsService;

    @Value("classpath:prompts/StressBurnoutPrompt.st")
    private Resource stressBurnoutPrompt;

    @Value("classpath:prompts/DepressionAnxietyPrompt.st")
    private Resource depressionAnxietyPrompt;

    @Value("classpath:prompts/AttachmentPrompt.st")
    private Resource attachmentPrompt;

    @Override
    public String createSBPrompt(Long userId, Long reportId) {
        ReportsResponse.IntegratedReportDetail detail = stressReportsService.getReportDetail(reportId, userId);

        // 유저 찾기
        String userName = userRepository.findById(userId).
                orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND)).getNickname();

        // 데이터 정규화
        String insights = (detail.getAiInsights() != null && !detail.getAiInsights().isEmpty())
                ? String.join(", ", detail.getAiInsights())
                : "분석된 추가 통찰이 없습니다.";

        return new SystemPromptTemplate(stressBurnoutPrompt)
                .render(Map.of(
                        "user_name", userName,
                        "stress_level_label", detail.getStress().getStressLevel(),
                        "control_label", detail.getStress().getControlLevel(),
                        "overload_label", detail.getStress().getOverloadLevel(),
                        "exhaustion_label", detail.getBurnout().getExhaustionLevel(),
                        "cynicism_label", detail.getBurnout().getCynicismLevel(),
                        "inefficacy_label", detail.getBurnout().getInefficacyLevel(),
                        "ai_insight_list", insights.isEmpty() ? "특이사항 없음" : insights
                ));
    }

    @Override
    public String createDAPrompt(Long userId, Long reportId) {
        ReportsResponse.DepressionAnxietyReportDetail detail = depressionAnxietyReportsService.getReportDetail(reportId, userId);

        // 유저 찾기
        String userName = userRepository.findById(userId).
                orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND)).getNickname();

        // 1. 우울 클러스터 전체 현황 가공 (예: "핵심 증상(33%), 신체 증상(0%), 심리 증상(22%)")
        String depClusterSummary = detail.getPhq9().getClusters().stream()
                .map(c -> String.format("%s(%.0f%%)", c.getName(), c.getScoreRatio() * 100))
                .collect(Collectors.joining(", "));

        // 2. 불안 클러스터 전체 현황 가공
        String anxClusterSummary = detail.getGad7().getClusters().stream()
                .map(c -> String.format("%s(%.0f%%)", c.getName(), c.getScoreRatio() * 100))
                .collect(Collectors.joining(", "));

        // 3. AI 인사이트 가공
        String insights = detail.getAiInsightCards().stream()
                .map(ReportsResponse.AiInsightCard::getTitle)
                .collect(Collectors.joining(", "));

        return new SystemPromptTemplate(depressionAnxietyPrompt)
                .render(new HashMap<String, Object>() {{
                    put("user_name", userName);
                    put("dep_total_score", detail.getPhq9().getTotalScore());
                    put("dep_severity_label", detail.getSummary().getStatusLabel());
                    put("dep_cluster_description", depClusterSummary);
                    put("dep_ai_insights", insights);
                    put("dep_user_symptoms", detail.getDepressionAiContent());
                    put("q9_flag", detail.isEmergencyFlag());
                    put("anx_total_score", detail.getGad7().getTotalScore());
                    put("anx_severity_label", detail.getSummary().getStatusLabel());
                    put("anx_dominant_cluster", anxClusterSummary);
                    put("anx_ai_insights", insights);
                    put("anx_user_symptoms", detail.getAnxietyAiContent());
                }});
    }

    @Override
    public String createAPrompt(Long userId, Long reportId) {
        ReportsResponse.AttachmentReportDetail detail = attachmentReportsService.getReportDetail(reportId, userId);

        // 스트레스 및 강점 포인트 설명글 합치기 (List -> String)
        String stressDesc = detail.getStressPoints().stream()
                .map(point -> point.getTitle() + ": " + point.getDescription())
                .collect(Collectors.joining(" "));

        String strengthDesc = detail.getStrengthPoints().stream()
                .map(point -> point.getTitle() + ": " + point.getDescription())
                .collect(Collectors.joining(" "));

        // 3. 프롬프트 렌더링
        return new SystemPromptTemplate(attachmentPrompt)
                .render(Map.of(
                        "user_name", detail.getUserName(),
                        "attachment_type", detail.getAttachmentType(),
                        "anxiety_score_label", detail.getAnxiety().getStateLabel() + "(" + detail.getAnxiety().getStateComment() + ")",
                        "avoidance_score_label", detail.getAvoidance().getStateLabel() + "(" + detail.getAvoidance().getStateComment() + ")",
                        "stress_description", stressDesc,
                        "strength_description", strengthDesc,
                        "action_guide_sentence", detail.getActionGuideSentence()
                ));
    }
}
