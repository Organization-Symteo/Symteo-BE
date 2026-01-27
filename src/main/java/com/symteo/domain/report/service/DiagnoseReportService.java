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

        Optional<Reports> existingReport = reportsRepository.findByDuplicateCheck(
                user,
                diagnose.getTestType(),
                diagnose.getCreatedAt()
        );

        if (existingReport.isPresent()) {
            // 이미 존재한다면 새로 생성 x
            // 기존 리포트 정보 불러오기
            Reports report = existingReport.get();
            return ReportsResponse.CreateReportResult.builder()
                    .reportId(report.getReportId())
                    .testType(report.getRType())
                    .createdAt(report.getCreatedAt())
                    .build();
        }

        // 공통 리포트(부모) 생성 및 저장
        Reports report = Reports.builder()
                .user(user)
                .rType(diagnose.getTestType())
                .build();
        reportsRepository.save(report);

        // 답변 리스트 추출 및 총점 계산
        List<DiagnoseReqDTO.AnswerDTO> answers = diagnose.getAnswers();
        int totalScore = answers.stream()
                .mapToInt(a -> a.score().intValue())
                .sum();

        String prompt = "";

        // 테스트 유형별 상세 분석 로직 및 프롬프트 조립
        if ("PHQ-9".equals(diagnose.getTestType())) {
            DepressionReports deReport = processDepression(user, report, answers, totalScore);
            prompt = buildDepressionPrompt(user.getNickname(), deReport);
        }
        else if ("GAD-7".equals(diagnose.getTestType())) {
            AnxietyReports anReport = processAnxiety(user, report, answers, totalScore);
            prompt = buildAnxietyPrompt(user.getNickname(), anReport);
        }

        // AI API 호출 및 줄글 분석 결과 저장
        String aiResponse = aiModelService.callAiApi(prompt);
        DiagnoseAiReports aiReport = DiagnoseAiReports.builder()
                .user(user)
                .report(report)
                .aiContents(aiResponse)
                .build();
        aiReportsRepository.save(aiReport);

        // 리포트 완료 처리
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

        DiagnoseAiReports aiReport = aiReportsRepository.findByReport(report).orElse(null);

        Object scores = "PHQ-9".equals(report.getRType()) ?
                depressionRepository.findByReport(report).orElse(null) :
                anxietyRepository.findByReport(report).orElse(null);

        return ReportsResponse.ReportDetail.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .aiContents(aiReport != null ? aiReport.getAiContents() : "분석 내용을 찾을 수 없습니다.")
                .scores(scores)
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 우울(PHQ-9) 분석 상세 로직
    private DepressionReports processDepression(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int core = sumByNos(answers, 1, 2);
        int physical = sumByNos(answers, 3, 4, 5);
        int psychic = sumByNos(answers, 6, 7, 8, 9);
        boolean q9 = answers.stream().anyMatch(a -> a.questionNo() == 9 && a.score() >= 1);

        return depressionRepository.save(DepressionReports.builder()
                        .user(user)
                .report(report)
                .depressionScore(total)
                .severity(calculateSeverity(total, "PHQ-9"))
                .coreScore(core)
                .physicalScore(physical)
                .psychicScore(psychic)
                .isSafetyFlow(q9)
                .build());
    }

    // 불안(GAD-7) 분석 상세 로직
    private AnxietyReports processAnxiety(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers, int total) {
        int emotional = sumByNos(answers, 1, 2, 3);
        int tension = sumByNos(answers, 4, 5, 6, 7);

        return anxietyRepository.save(AnxietyReports.builder()
                        .user(user)
                .report(report)
                .anxietyScore(total)
                .severity(calculateSeverity(total, "GAD-7"))
                .emotionalScore(emotional)
                .tensionScore(tension)
                .build());
    }

    // 유틸리티 및 계산 메서드

    private int sumByNos(List<DiagnoseReqDTO.AnswerDTO> answers, int... nos) {
        Set<Long> targetNos = Arrays.stream(nos).mapToLong(i -> (long)i).boxed().collect(Collectors.toSet());
        return answers.stream()
                .filter(a -> targetNos.contains(a.questionNo()))
                .mapToInt(a -> a.score().intValue())
                .sum();
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

    // 프롬프트 생성

    private String buildDepressionPrompt(String name, DepressionReports de) {
        return String.format(
                "Role: 당신은 '심터'의 AI 심리 분석가입니다. [사용자: %s, 상태: %s, 점수: %d]\n" +
                        "지침: 10~12문장 줄글 작성. '%s님의 현재 우울 상태는 %s 단계입니다'로 시작하세요.\n" +
                        "안전망: 자살사고 위험성(%b)이 있다면 마지막에 전문가 도움 권유 메시지를 반드시 포함하세요.",
                name, de.getSeverity(), de.getDepressionScore(), name, de.getSeverity(), de.getIsSafetyFlow()
        );
    }

    private String buildAnxietyPrompt(String name, AnxietyReports an) {
        String dominant = an.getEmotionalScore() >= an.getTensionScore() ? "정서적 불안" : "신체적 긴장";
        return String.format(
                "Role: 당신은 '심터'의 AI 심리 분석가입니다. [사용자: %s, 상태: %s, 우세영역: %s]\n" +
                        "지침: 데이터 기반 인과관계 분석 위주로 12문장 이내 줄글로 작성하세요.",
                name, an.getSeverity(), dominant
        );
    }
}