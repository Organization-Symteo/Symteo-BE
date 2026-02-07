package com.symteo.domain.report.service;

import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.enums.DiagnoseType;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.AttachmentReports;
import com.symteo.domain.report.entity.mapping.Strength;
import com.symteo.domain.report.entity.mapping.StressPoints;
import com.symteo.domain.report.repository.AttachmentReportsRepository;
import com.symteo.domain.report.repository.ReportsRepository;
import com.symteo.domain.report.repository.StressPointsRepository;
import com.symteo.domain.report.repository.StrengthRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentReportsService {

    private final ReportsRepository reportsRepository;
    private final AttachmentReportsRepository attachmentReportsRepository;
    private final StressPointsRepository stressPointsRepository;
    private final StrengthRepository strengthRepository;
    private final AiModelService aiModelService;
    private final UserRepository userRepository;

    public ReportsResponse.CreateReportResult analyzeAndSave(Diagnose diagnose, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 1. 점수 계산 및 유형 판정
        Map<Integer, Double> scores = preprocessAnswers(diagnose.getAnswers());
        double anxietyScore = calculateAverage(scores, List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 29, 30, 32, 34, 36));
        double avoidanceScore = calculateAverage(scores, List.of(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 31, 33, 35));
        String type = determineType(anxietyScore, avoidanceScore);

        // 2. DB 고정 문구 조회
        List<StressPoints> stressList = stressPointsRepository.findByAttachmentType(type);
        List<Strength> strengthList = strengthRepository.findByAttachmentType(type);

        // 3. 행동 제언 및 AI 프롬프트 생성 (줄바꿈 제거 지침 포함)
        String actionGuide = getActionGuideSentence(type);
        String prompt = buildAttachmentPrompt(user.getNickname(), type,
                getScoreLabel(anxietyScore), getScoreLabel(avoidanceScore),
                stressList.get(0).getStContents(), strengthList.get(0).getStrengthContents(), actionGuide);

        String aiResultText = aiModelService.callAiApi(prompt);

        // 4. 리포트 마스터 및 상세 저장
        Reports report = reportsRepository.save(Reports.builder()
                .user(user).diagnoseId(diagnose.getId()).rType(DiagnoseType.ATTACHMENT_TEST).build());

        attachmentReportsRepository.save(AttachmentReports.builder()
                .user(user).report(report)
                .anxietyScore(anxietyScore).avoidanceScore(avoidanceScore)
                .attachmentType(type).aiFullContent(aiResultText)
                .stressPoint1(stressList.get(0)).stressPoint2(stressList.get(1))
                .strength1(strengthList.get(0)).strength2(strengthList.get(1))
                .actionGuideSentence(actionGuide).build());

        report.complete();
        return ReportsResponse.CreateReportResult.builder()
                .reportId(report.getReportId()).testType(DiagnoseType.ATTACHMENT_TEST)
                .createdAt(report.getCreatedAt()).build();
    }

    @Transactional(readOnly = true)
    public ReportsResponse.AttachmentReportDetail getReportDetail(Long reportId, Long userId) {
        Reports report = reportsRepository.findReportWithDetails(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._REPORT_NOT_FOUND));

        AttachmentReports at = report.getAttachmentReport();
        return ReportsResponse.AttachmentReportDetail.builder()
                .reportId(report.getReportId()).userName(report.getUser().getNickname())
                .attachmentType(at.getAttachmentType())
                .anxiety(getScoreMetadata(at.getAnxietyScore(), true))
                .avoidance(getScoreMetadata(at.getAvoidanceScore(), false))
                .stressPoints(List.of(
                        new ReportsResponse.AttachmentCard(at.getStressPoint1().getTitle(), at.getStressPoint1().getStContents()),
                        new ReportsResponse.AttachmentCard(at.getStressPoint2().getTitle(), at.getStressPoint2().getStContents())
                ))
                .strengthPoints(List.of(
                        new ReportsResponse.AttachmentCard(at.getStrength1().getTitle(), at.getStrength1().getStrengthContents()),
                        new ReportsResponse.AttachmentCard(at.getStrength2().getTitle(), at.getStrength2().getStrengthContents())
                ))
                .aiFullContent(at.getAiFullContent())
                .actionGuideSentence(at.getActionGuideSentence())
                .createdAt(report.getCreatedAt()).build();
    }

    // --- 수치 계산 및 유형 판정 로직 ---
    private Map<Integer, Double> preprocessAnswers(List<com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO.AnswerDTO> answers) {
        Set<Integer> reverseItems = Set.of(3, 15, 19, 22, 25, 32);
        return answers.stream().collect(Collectors.toMap(
                a -> a.questionNo().intValue(),
                a -> reverseItems.contains(a.questionNo().intValue()) ? 6.0 - a.score() : (double) a.score()
        ));
    }

    private double calculateAverage(Map<Integer, Double> scores, List<Integer> nos) {
        double sum = nos.stream().mapToDouble(n -> scores.getOrDefault(n, 3.0)).sum();
        return Math.round((sum / nos.size()) * 100) / 100.0;
    }

    private String determineType(double anxiety, double avoidance) {
        if (anxiety < 3.0 && avoidance < 3.0) return "안정형";
        if (anxiety >= 3.0 && avoidance < 3.0) return "불안형";
        if (anxiety < 3.0 && avoidance >= 3.0) return "거부 회피형";
        return "공포 회피형";
    }

    private ReportsResponse.AttachmentScore getScoreMetadata(double score, boolean isAnxiety) {
        int percent = (int) ((score - 1) / 4.0 * 100);
        String label, color, comment;
        if (percent <= 25) { label = "매우 낮음"; color = "#63B19B"; comment = isAnxiety ? "정서적으로 안정적이고 신뢰감이 높아요." : "친밀한 관계 형성에 비교적 적극적이에요."; }
        else if (percent <= 50) { label = "낮음"; color = "#FFE8A9"; comment = isAnxiety ? "관계에서 비교적 편안함을 느껴요." : "가까움과 거리 사이의 균형을 유지해요."; }
        else if (percent <= 75) { label = "보통"; color = "#FFAC79"; comment = isAnxiety ? "상대의 반응에 조금 민감해지는 편이에요." : "개인적인 공간이 필요해지는 편이에요."; }
        else { label = "높음"; color = "#F4574F"; comment = isAnxiety ? "상대의 반응에 많이 예민해요." : "심리적 거리를 두고 싶어져요."; }
        return ReportsResponse.AttachmentScore.builder().score(score).percentage(percent).stateLabel(label).color(color).stateComment(comment).build();
    }

    private String getActionGuideSentence(String type) {
        return switch (type) {
            case "안정형" -> "지금처럼 솔직하고 유연한 소통 방식을 유지해 보세요. 건강한 마음은 주변 사람들에게 긍정적인 에너지를 전달하는 좋은 리더가 될 수 있는 밑거름이 됩니다.";
            case "불안형" -> "자신만의 시간을 즐기는 연습이 필요합니다. 상대의 반응과 나의 가치를 분리해 보는 것부터 시작해 볼까요? 당신은 그 자체로 충분히 소중한 사람입니다.";
            case "거부 회피형" -> "가까운 사람에게 자신의 취약한 면을 조금씩 공유해 보세요. 관계가 더 깊어지고 마음이 한결 가벼워지는 특별한 경험을 하게 될 것입니다.";
            case "공포 회피형" -> "작은 신뢰부터 차근차근 쌓아가는 경험이 중요합니다. 나를 진심으로 믿어주는 한 사람을 정해 마음의 문을 조금만 열어보는 건 어떨까요?";
            default -> "";
        };
    }

    private String getScoreLabel(double score) { return score >= 4.0 ? "높음" : (score >= 3.0 ? "보통" : "낮음"); }

    private String buildAttachmentPrompt(String name, String type, String anLabel, String avLabel, String stDesc, String strDesc, String guide) {
        return String.format(
                "당신은 따뜻한 공감을 전하는 전문 심리 상담사입니다. 아래 데이터를 바탕으로 분석 리포트를 작성하세요.\n\n" +
                        "[데이터]\n- 사용자: %s\n- 애착유형: %s\n- 불안 상태: %s\n- 회피 상태: %s\n- 스트레스 요인: %s\n- 내면의 강점: %s\n- 행동 제언: %s\n\n" +
                        "[작성 지침 - 필독]\n" +
                        "1. 문단 구분이나 줄바꿈(\\n)을 절대 사용하지 마세요. 모든 내용을 하나의 연속된 긴 문단으로 작성하세요.\n" +
                        "2. 시작은 반드시 \"%s님의 애착유형은 '%s'입니다.\"로 하세요.\n" +
                        "3. 스트레스 요인(%s)에 대해 공감한 뒤, 그것이 %s라는 강점에서 비롯된 것임을 설명하여 위로를 전하세요.\n" +
                        "4. 마지막은 행동 제언(%s)을 자연스럽게 포함하여 응원의 메시지로 마무리하세요.\n" +
                        "5. 마크다운, 볼드체 없이 오직 순수 텍스트만 출력하세요.",
                name, type, anLabel, avLabel, stDesc, strDesc, guide,
                name, type, stDesc, strDesc, guide
        );
    }
}