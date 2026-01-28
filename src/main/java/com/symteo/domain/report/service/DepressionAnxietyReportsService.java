package com.symteo.domain.report.service;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.repository.DiagnoseRepository;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.entity.DiagnoseAiReports;
import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.AnxietyReports;
import com.symteo.domain.report.entity.mapping.DepressionReports;
import com.symteo.domain.report.repository.*;
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
public class DepressionAnxietyReportsService {

    private final UserRepository userRepository;
    private final ReportsRepository reportsRepository;
    private final DepressionReportsRepository depressionRepository;
    private final AnxietyReportsRepository anxietyRepository;
    private final AiReportsRepository aiReportsRepository;
    private final AiModelService aiModelService;
    private final DiagnoseRepository diagnoseRepository;

    // 우울/불안 리포트 생성 api
    public ReportsResponse.CreateReportResult analyzeAndSave(Diagnose diagnose, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 중복 체크 (diagnoseId 및 rType 기준)
        Optional<Reports> existingReport = reportsRepository.findByDuplicateCheck(
                user, "DEPRESSION_ANXIETY_COMPLEX", diagnose.getId());

        if (existingReport.isPresent()) {
            return ReportsResponse.CreateReportResult.builder()
                    .reportId(existingReport.get().getReportId())
                    .testType(existingReport.get().getRType())
                    .createdAt(existingReport.get().getCreatedAt())
                    .build();
        }

        // 리포트 마스터 생성
        Reports report = reportsRepository.save(Reports.builder()
                .user(user).diagnoseId(diagnose.getId())
                .rType("DEPRESSION_ANXIETY_COMPLEX").build());

        // 점수 계산 및 개별 도메인 저장
        List<DiagnoseReqDTO.AnswerDTO> answers = diagnose.getAnswers();
        int deTotal = calculateTotalByRange(answers, 1, 9);
        int anTotal = calculateTotalByRange(answers, 10, 16);

        DepressionReports deReport = processDepression(user, report, answers, deTotal);
        AnxietyReports anReport = processAnxiety(user, report, answers, anTotal);

        // AI 통합 분석문 생성
        String dePrompt = buildDepressionPrompt(user.getNickname(), deReport, answers);
        String anPrompt = buildAnxietyPrompt(user.getNickname(), anReport, answers);
        String finalAiContents = aiModelService.callAiApi(dePrompt) + "||" + aiModelService.callAiApi(anPrompt);

        aiReportsRepository.save(DiagnoseAiReports.builder()
                .user(user).report(report).aiContents(finalAiContents).build());

        report.complete();
        return ReportsResponse.CreateReportResult.builder()
                .reportId(report.getReportId()).testType(report.getRType())
                .createdAt(report.getCreatedAt()).build();
    }

    // 우울/불안 리포트 조회
    @Transactional(readOnly = true)
    public ReportsResponse.DepressionAnxietyReportDetail getReportDetail(Long reportId, Long userId) {
        // fetch join을 활용한 데이터 통합 조회
        Reports report = reportsRepository.findReportWithDetails(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._REPORT_NOT_FOUND));

        if (!report.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        DepressionReports de = report.getDepressionReport();
        AnxietyReports an = report.getAnxietyReport();
        DiagnoseAiReports ai = report.getAiReport();
        Diagnose diagnose = diagnoseRepository.findById(report.getDiagnoseId()).orElseThrow();

        // 종합 결과 계산 (A = (S1 + S2) / 2)
        double avg = (de.getDepressionScore() + an.getAnxietyScore()) / 2.0;

        // AI 분석 본문 분리 로직
        String[] aiParts = (ai != null && ai.getAiContents().contains("||"))
                ? ai.getAiContents().split("\\|\\|") : new String[]{ai != null ? ai.getAiContents() : "", ""};

        return ReportsResponse.DepressionAnxietyReportDetail.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .summary(ReportsResponse.OverallSummary.builder()
                        .averageScore(avg)
                        .statusLabel(calculateOverallLabel(avg))
                        .statusColor(getColorByRatio(avg / 24.0)).build())
                .phq9(buildPhqSection(de))
                .gad7(buildGadSection(an))
                .aiInsightCards(extractInsightCards(diagnose.getAnswers()))
                .depressionAiContent(aiParts[0].trim())
                .anxietyAiContent(aiParts.length > 1 ? aiParts[1].trim() : "")
                .emergencyFlag(de.getIsSafetyFlow())
                .createdAt(report.getCreatedAt()).build();
    }

    // 시각화 데이터 산출 헬퍼 메서드

    private String calculateOverallLabel(double avg) {
        if (avg >= 20) return "즉시 도움 필요";
        if (avg >= 15) return "관리 필요";
        if (avg >= 10) return "주의";
        if (avg >= 5) return "양호";
        return "안정";
    }

    private String getColorByRatio(double ratio) {
        if (ratio >= 0.76) return "#F4574F"; // 빨강
        if (ratio >= 0.51) return "#FFAC79"; // 주황
        if (ratio >= 0.26) return "#FFE8A9"; // 노랑
        return "#63B19B"; // 초록
    }

    private ReportsResponse.PhqDetail buildPhqSection(DepressionReports de) {
        return ReportsResponse.PhqDetail.builder()
                .totalScore(de.getDepressionScore())
                .needleDeg((de.getDepressionScore() / 27.0) * 180.0)
                .clusters(List.of(
                        createCluster("핵심 증상", de.getCoreScore(), 6.0),
                        createCluster("신체 증상", de.getPhysicalScore(), 9.0),
                        createCluster("심리 증상", de.getPsychicScore(), 9.0)
                )).build();
    }

    private ReportsResponse.GadDetail buildGadSection(AnxietyReports an) {
        return ReportsResponse.GadDetail.builder()
                .totalScore(an.getAnxietyScore())
                .needleDeg((an.getAnxietyScore() / 21.0) * 180.0)
                .clusters(List.of(
                        createCluster("정서적 불안", an.getEmotionalScore(), 9.0),
                        createCluster("신체적 긴장", an.getTensionScore(), 12.0)
                )).build();
    }

    private ReportsResponse.ClusterResult createCluster(String name, int score, double max) {
        double ratio = score / max;
        return ReportsResponse.ClusterResult.builder()
                .name(name).scoreRatio(ratio).color(getColorByRatio(ratio)).build();
    }

    private List<ReportsResponse.AiInsightCard> extractInsightCards(List<DiagnoseReqDTO.AnswerDTO> answers) {
        List<ReportsResponse.AiInsightCard> cards = new ArrayList<>();
        for (DiagnoseReqDTO.AnswerDTO a : answers) {
            if (a.score() >= 2) {
                switch (a.questionNo().intValue()) {
                    case 3 -> cards.add(new ReportsResponse.AiInsightCard("sleep_issue", "수면 장애 심각"));
                    case 11 -> cards.add(new ReportsResponse.AiInsightCard("worry_issue", "지속적인 걱정"));
                    case 15 -> cards.add(new ReportsResponse.AiInsightCard("tension_issue", "신체적 긴장"));
                    case 7 -> cards.add(new ReportsResponse.AiInsightCard("focus_issue", "집중력 저하"));
                }
            }
        }
        return cards;
    }

    // 수치 계산 및 데이터 저장 헬퍼
    private DepressionReports processDepression(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int core = sumByNos(answers, 1, 2);
        int physical = sumByNos(answers, 3, 4, 5);
        int psychic = sumByNos(answers, 6, 7, 8, 9);
        boolean q9 = answers.stream().anyMatch(a -> a.questionNo() == 9 && a.score() >= 1);
        return depressionRepository.save(DepressionReports.builder()
                .user(user).report(report).depressionScore(total).severity(calculateSeverity(total, "PHQ-9"))
                .coreScore(core).physicalScore(physical).psychicScore(psychic).isSafetyFlow(q9).build());
    }

    private AnxietyReports processAnxiety(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int emotional = sumByNos(answers, 10, 11, 12);
        int tension = sumByNos(answers, 13, 14, 15, 16);
        return anxietyRepository.save(AnxietyReports.builder()
                .user(user).report(report).anxietyScore(total).severity(calculateSeverity(total, "GAD-7"))
                .emotionalScore(emotional).tensionScore(tension).build());
    }

    // AI 프롬프트 생성 및 유틸리티

    private String buildDepressionPrompt(String name, DepressionReports de, List<DiagnoseReqDTO.AnswerDTO> answers) {
        String symptoms = answers.stream().filter(a -> a.questionNo() <= 9 && a.score() >= 2)
                .map(this::mapQuestionToSymptom).collect(Collectors.joining(", "));
        return String.format("AI 분석가로서 %s님의 우울 상태('%s')를 데이터 기반으로 분석하세요. 주요 증상(%s)을 포함해 10문장 이내 줄글로 작성하고 마크다운은 금지합니다.",
                name, de.getSeverity(), symptoms);
    }

    private String buildAnxietyPrompt(String name, AnxietyReports an, List<DiagnoseReqDTO.AnswerDTO> answers) {
        String symptoms = answers.stream().filter(a -> a.questionNo() >= 10 && a.score() >= 2)
                .map(this::mapQuestionToSymptom).collect(Collectors.joining(", "));
        return String.format("AI 분석가로서 %s님의 불안 상태('%s')를 분석하세요. 증상(%s) 위주로 10문장 이내 줄글로 작성하세요.",
                name, an.getSeverity(), symptoms);
    }

    private String mapQuestionToSymptom(DiagnoseReqDTO.AnswerDTO a) {
        return switch (a.questionNo().intValue()) {
            case 1 -> "흥미 저하"; case 2 -> "우울감"; case 3 -> "수면 장애"; case 7 -> "집중력 저하";
            case 11 -> "걱정 조절 불가"; case 15 -> "안절부절못함"; default -> "기타 증상";
        };
    }

    private int sumByNos(List<DiagnoseReqDTO.AnswerDTO> answers, int... nos) {
        Set<Long> targets = Arrays.stream(nos).mapToLong(i -> (long)i).boxed().collect(Collectors.toSet());
        return answers.stream().filter(a -> targets.contains(a.questionNo())).mapToInt(a -> a.score().intValue()).sum();
    }

    private int calculateTotalByRange(List<DiagnoseReqDTO.AnswerDTO> answers, int start, int end) {
        return answers.stream().filter(a -> a.questionNo() >= start && a.questionNo() <= end).mapToInt(a -> a.score().intValue()).sum();
    }

    private String calculateSeverity(int score, String type) {
        if ("PHQ-9".equals(type)) {
            if (score <= 4) return "최소"; if (score <= 9) return "경도";
            if (score <= 14) return "중등도"; if (score <= 19) return "중등고도"; return "고도";
        } else {
            if (score <= 4) return "최소"; if (score <= 9) return "경도";
            if (score <= 14) return "중등도"; return "고도";
        }
    }
}