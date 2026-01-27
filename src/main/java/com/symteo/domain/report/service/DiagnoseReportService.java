package com.symteo.domain.report.service;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.entity.Diagnose;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnoseReportService {

    private final UserRepository userRepository;
    private final ReportsRepository reportsRepository;
    private final DepressionReportsRepository depressionRepository;
    private final AnxietyReportsRepository anxietyRepository;
    private final AiReportsRepository aiReportsRepository;
    private final AiModelService aiModelService;

    // 우울/불안 리포트 생성 및 저장 api
    public ReportsResponse.CreateReportResult analyzeAndSave(Diagnose diagnose, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 리포트가 이미 존재하면 생성 x, 내용 조회
        Optional<Reports> existingReport = reportsRepository.findByDuplicateCheck(user, diagnose.getTestType(), diagnose.getCreatedAt());
        if (existingReport.isPresent()) {
            return ReportsResponse.CreateReportResult.builder()
                    .reportId(existingReport.get().getReportId())
                    .testType(existingReport.get().getRType())
                    .createdAt(existingReport.get().getCreatedAt())
                    .build();
        }

        Reports report = Reports.builder()
                .user(user)
                .rType("DEPRESSION_ANXIETY_COMPLEX")
                .build();
        reportsRepository.save(report);

        List<DiagnoseReqDTO.AnswerDTO> answers = diagnose.getAnswers();
        int deTotal = calculateTotalByRange(answers, 1, 9);
        int anTotal = calculateTotalByRange(answers, 10, 16);

        DepressionReports deReport = processDepression(user, report, answers, deTotal);
        AnxietyReports anReport = processAnxiety(user, report, answers, anTotal);

        // AI에게 두 결과를 하나로 합치되 중간에 ||를 넣으라고 명령하는 통합 프롬프트 방식
        String dePrompt = buildDepressionPrompt(user.getNickname(), deReport, answers);
        String anPrompt = buildAnxietyPrompt(user.getNickname(), anReport, answers);

        // 각각 호출 후 서버에서 강제로 || 붙여서 저장
        String deAiResponse = aiModelService.callAiApi(dePrompt);
        String anAiResponse = aiModelService.callAiApi(anPrompt);

        // DB 저장 시 구분자 삽입
        String finalContents = deAiResponse + "||" + anAiResponse;

        aiReportsRepository.save(DiagnoseAiReports.builder()
                .user(user)
                .report(report)
                .aiContents(finalContents)
                .build());

        report.complete();
        return ReportsResponse.CreateReportResult.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 우울/불안 리포트 조회 api
    @Transactional(readOnly = true)
    public ReportsResponse.ReportDetail getReportDetail(Long reportId, Long userId) {
        Reports report = reportsRepository.findById(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._REPORT_NOT_FOUND));

        if (!report.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._MEMBER_NOT_FOUND);
        }

        DepressionReports de = depressionRepository.findByReport(report).orElse(null);
        AnxietyReports an = anxietyRepository.findByReport(report).orElse(null);
        DiagnoseAiReports ai = aiReportsRepository.findByReport(report).orElse(null);

        String deAi = "분석 내용을 생성 중이거나 찾을 수 없습니다.";
        String anAi = "분석 내용을 생성 중이거나 찾을 수 없습니다.";

        // [조회] 저장된 ||를 기준으로 분리하여 DTO에 각각 매핑
        if (ai != null && ai.getAiContents() != null) {
            String raw = ai.getAiContents();
            if (raw.contains("||")) {
                String[] parts = raw.split("\\|\\|");
                deAi = parts[0].trim();
                anAi = (parts.length > 1) ? parts[1].trim() : "";
            } else {
                // 구분자가 없는 기존 데이터 대응
                deAi = raw;
            }
        }

        return ReportsResponse.ReportDetail.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .depressionAiContent(deAi)
                .anxietyAiContent(anAi)
                .depressionScores(de)
                .anxietyScores(an)
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 수치 계산 로직
    private DepressionReports processDepression(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int core = sumByNos(answers, 1, 2);
        int physical = sumByNos(answers, 3, 4, 5);
        int psychic = sumByNos(answers, 6, 7, 8, 9);
        boolean q9 = answers.stream().anyMatch(a -> a.questionNo() == 9 && a.score() >= 1);

        return depressionRepository.save(DepressionReports.builder()
                .user(user).report(report).depressionScore(total)
                .severity(calculateSeverity(total, "PHQ-9"))
                .coreScore(core).physicalScore(physical).psychicScore(psychic).isSafetyFlow(q9)
                .build());
    }

    private AnxietyReports processAnxiety(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int emotional = sumByNos(answers, 10, 11, 12);
        int tension = sumByNos(answers, 13, 14, 15, 16);

        return anxietyRepository.save(AnxietyReports.builder()
                .user(user).report(report).anxietyScore(total)
                .severity(calculateSeverity(total, "GAD-7"))
                .emotionalScore(emotional).tensionScore(tension)
                .build());
    }

    // 프롬프트 생성 로직 (마크다운 제거 및 텍스트 최적화)
    // 우울 프롬프트 상세 수정
    private String buildDepressionPrompt(String name, DepressionReports de, List<DiagnoseReqDTO.AnswerDTO> answers) {
        // 사용자가 2점 이상(자주 겪음) 체크한 실제 증상 키워드 매핑
        String userSymptomsText = answers.stream()
                .filter(a -> a.questionNo() <= 9 && a.score() >= 2)
                .map(this::mapQuestionToSymptom) // 문항 번호를 실제 증상 텍스트로 변환
                .collect(Collectors.joining(", "));

        return String.format(
                "아래 데이터를 바탕으로 '심터' 앱의 AI 심리 분석가로서 줄글 리포트를 작성하세요.\n\n" +
                        "[입력 데이터]\n" +
                        "- 사용자 닉네임: %s\n" +
                        "- PHQ-9 결과: 총점 %d점, 상태 등급 '%s'\n" +
                        "- 주요 증상 점수: 핵심(%d), 신체(%d), 심리(%d)\n" +
                        "- 사용자 고득점 증상: %s\n" +
                        "- 위험 문항(자살 사고) 응답 여부: %b\n\n" +
                        "[작성 지침]\n" +
                        "1. \"%s님의 현재 우울 상태는 '%s' 단계입니다.\"로 시작할 것.\n" +
                        "2. 사용자가 직접 체크한 증상(%s)을 언급하며, 이것이 현재 상태와 어떻게 연결되는지 '데이터 기반'으로 해석할 것.\n" +
                        "3. 10문장 이내의 자연스러운 줄글로 작성하고 마크다운이나 볼드체는 절대 사용 금지.\n" +
                        "4. 위험 문항이 True일 경우: \"혹시 혼자 감당하기 버거운 나쁜 생각이 든다면, 망설이지 말고 전문가에게 도움을 요청해 주세요. 당신은 혼자가 아닙니다.\" 문구를 마지막에 반드시 포함할 것.",
                name, de.getDepressionScore(), de.getSeverity(), de.getCoreScore(), de.getPhysicalScore(), de.getPsychicScore(),
                userSymptomsText, de.getIsSafetyFlow(), name, de.getSeverity(), userSymptomsText
        );
    }

    // 불안 프롬프트 상세 수정
    private String buildAnxietyPrompt(String name, AnxietyReports an, List<DiagnoseReqDTO.AnswerDTO> answers) {
        String dominantCluster = an.getEmotionalScore() >= an.getTensionScore() ? "정서적 불안" : "신체적 긴장";
        String userSymptomsText = answers.stream()
                .filter(a -> a.questionNo() >= 10 && a.score() >= 2)
                .map(this::mapQuestionToSymptom)
                .collect(Collectors.joining(", "));

        return String.format(
                "아래 데이터를 바탕으로 '심터' 앱의 AI 심리 분석가로서 줄글 리포트를 작성하세요.\n\n" +
                        "[입력 데이터]\n" +
                        "- 사용자 닉네임: %s\n" +
                        "- GAD-7 결과: 총점 %d점, 상태 등급 '%s'\n" +
                        "- 우세 영역: %s\n" +
                        "- 사용자 고득점 증상: %s\n\n" +
                        "[작성 지침]\n" +
                        "1. \"%s님의 현재 불안 상태는 '%s' 단계입니다.\"로 시작할 것.\n" +
                        "2. 우세 영역(%s)과 고득점 증상을 연결하여 인과관계 위주로 설명할 것.\n" +
                        "3. 10문장 이내의 줄글로 작성하고 전문가의 도움 권유 메시지를 포함할 것.\n" +
                        "4. 마크다운이나 특수문자 없이 텍스트만 출력할 것.",
                name, an.getAnxietyScore(), an.getSeverity(), dominantCluster, userSymptomsText,
                name, an.getSeverity(), dominantCluster
        );
    }

    // 문항 번호를 사람이 읽기 좋은 증상명으로 변환하는 헬퍼 메서드
    private String mapQuestionToSymptom(DiagnoseReqDTO.AnswerDTO a) {
        return switch (a.questionNo().intValue()) {
            case 1 -> "흥미 저하"; case 2 -> "우울감"; case 3 -> "수면 장애";
            case 4 -> "피로감"; case 5 -> "식욕 변화"; case 6 -> "자책감";
            case 7 -> "집중력 저하"; case 8 -> "초조함 또는 느려짐"; case 9 -> "자해 사고";
            case 10 -> "초조함"; case 11 -> "걱정 조절 불가"; case 12 -> "과도한 걱정";
            case 13 -> "안절부절못함"; case 14 -> "긴장 이완 불가"; case 15 -> "쉽게 짜증남";
            case 16 -> "불길한 예감";
            default -> "기타 증상";
        };
    }

    // 유틸리티 메서드
    private int sumByNos(List<DiagnoseReqDTO.AnswerDTO> answers, int... nos) {
        Set<Long> targetNos = Arrays.stream(nos).mapToLong(i -> (long)i).boxed().collect(Collectors.toSet());
        return answers.stream().filter(a -> targetNos.contains(a.questionNo())).mapToInt(a -> a.score().intValue()).sum();
    }

    private int calculateTotalByRange(List<DiagnoseReqDTO.AnswerDTO> answers, int start, int end) {
        return answers.stream().filter(a -> a.questionNo() >= start && a.questionNo() <= end).mapToInt(a -> a.score().intValue()).sum();
    }

    private String calculateSeverity(int score, String type) {
        if ("PHQ-9".equals(type)) {
            if (score <= 4) return "최소";
            if (score <= 9) return "경도";
            if (score <= 14) return "중등도";
            if (score <= 19) return "중등고도";
            return "고도";
        } else {
            if (score <= 4) return "최소";
            if (score <= 9) return "경도";
            if (score <= 14) return "중등도";
            return "고도";
        }
    }
}