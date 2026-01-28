package com.symteo.domain.report.service;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.entity.DiagnoseAiReports;
import com.symteo.domain.report.entity.Reports;
import com.symteo.domain.report.entity.mapping.BurnoutReports;
import com.symteo.domain.report.entity.mapping.StressReports;
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
public class StressReportsService {

    private final UserRepository userRepository;
    private final ReportsRepository reportsRepository;
    private final StressReportsRepository stressReportsRepository;
    private final BurnoutReportsRepository burnoutReportsRepository;
    private final AiReportsRepository aiReportsRepository;
    private final AiModelService aiModelService;

    // 스트레스/번아웃 리포트 생성 api
    public ReportsResponse.CreateReportResult analyzeAndSave(Diagnose diagnose, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 중복 체크
        Optional<Reports> existingReport = reportsRepository.findByDuplicateCheck(
                user, diagnose.getTestType(), diagnose.getId());

        if (existingReport.isPresent()) {
            return ReportsResponse.CreateReportResult.builder()
                    .reportId(existingReport.get().getReportId())
                    .testType(existingReport.get().getRType())
                    .createdAt(existingReport.get().getCreatedAt())
                    .build();
        }

        Reports report = Reports.builder()
                .user(user)
                .diagnoseId(diagnose.getId())
                .rType("STRESS_BURNOUT_COMPLEX")
                .build();
        reportsRepository.save(report);

        List<DiagnoseReqDTO.AnswerDTO> answers = diagnose.getAnswers();

        // 개별 도메인 데이터 계산 및 저장
        StressReports stReport = processStress(user, report, answers);
        BurnoutReports buReport = processBurnout(user, report, answers);

        // 통합 지표 산출 (마음배터리 및 AI 인사이트)
        int batteryScore = calculateHeartBattery(stReport.getStressScore(), buReport.getTotalBurnoutScore());
        List<String> insights = generateDetailedInsights(stReport, buReport);

        // AI 통합 프롬프트 생성 (마음배터리와 인사이트 리스트 포함)
        String prompt = buildStressBurnoutPrompt(user.getNickname(), stReport, buReport, insights, batteryScore);
        String aiResponse = aiModelService.callAiApi(prompt);

        // AI 리포트 저장
        aiReportsRepository.save(DiagnoseAiReports.builder()
                .user(user)
                .report(report)
                .aiContents(aiResponse)
                .build());

        report.complete();
        return ReportsResponse.CreateReportResult.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 스트레스/번아웃 리포트 조회 api
    @Transactional(readOnly = true)
    public ReportsResponse.IntegratedReportDetail getReportDetail(Long reportId, Long userId) {
        Reports report = reportsRepository.findById(reportId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._REPORT_NOT_FOUND));

        if (!report.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        StressReports st = stressReportsRepository.findByReport(report).orElse(null);
        BurnoutReports bu = burnoutReportsRepository.findByReport(report).orElse(null);
        DiagnoseAiReports ai = aiReportsRepository.findByReport(report).orElse(null);

        int battery = (st != null && bu != null) ? calculateHeartBattery(st.getStressScore(), bu.getTotalBurnoutScore()) : 0;

        return ReportsResponse.IntegratedReportDetail.builder()
                .reportId(report.getReportId())
                .testType(report.getRType())
                .batteryPercent(battery)
                .batteryColor(getBatteryColor(battery))
                .batteryGuide(getBatteryGuide(battery))
                .stress(ReportsResponse.StressDetail.builder()
                        .pssScore(st.getStressScore())
                        .stressLevel(st.getStressLevel())
                        .controlLevel(st.getControlLevel())
                        .overloadLevel(st.getOverloadLevel())
                        .build())
                .burnout(ReportsResponse.BurnoutDetail.builder()
                        .exhaustionLevel(bu.getExhaustionLevel())
                        .cynicismLevel(bu.getCynicismLevel())
                        .inefficacyLevel(bu.getInefficacyLevel())
                        .totalLevel(bu.getTotalBurnoutLevel())
                        .build())
                .aiInsights(generateDetailedInsights(st, bu))
                .aiFullContent(ai != null ? ai.getAiContents() : "")
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 수치 계산 로직
    private StressReports processStress(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers) {
        // PSS 역채점 문항(4,5,6,7,8) 반영
        int total = answers.stream().filter(a -> a.questionNo() <= 10)
                .mapToInt(a -> List.of(4L, 5L, 6L, 7L, 8L).contains(a.questionNo()) ? 4 - a.score().intValue() : a.score().intValue()).sum();

        double control = calculatePercent(answers, List.of(4, 5, 6, 7, 8), 20);
        double overload = calculatePercent(answers, List.of(1, 2, 3, 9, 10), 20);

        return stressReportsRepository.save(StressReports.builder()
                .user(user).report(report).stressScore(total)
                .stressLevel(calculateStressLevel(total))
                .controlLevel(mapToLabel(control)).overloadLevel(mapToLabel(overload))
                .controlPercent(control).overloadPercent(overload)
                .build());
    }

    private BurnoutReports processBurnout(User user, Reports report, List<DiagnoseReqDTO.AnswerDTO> answers) {
        int total = answers.stream().filter(a -> a.questionNo() >= 11 && a.questionNo() <= 32)
                .mapToInt(a -> a.score().intValue()).sum();

        double exh = calculatePercent(answers, List.of(11, 12, 13, 14, 15, 16, 17, 18, 19), 36);
        double cyn = calculatePercent(answers, List.of(20, 21, 22, 23, 24, 25), 24);
        double inef = calculatePercent(answers, List.of(26, 27, 28, 29, 30, 31, 32), 28);

        return burnoutReportsRepository.save(BurnoutReports.builder()
                .user(user).report(report).totalBurnoutScore(total)
                .exhaustionLevel(mapToLabel(exh)).cynicismLevel(mapToLabel(cyn)).inefficacyLevel(mapToLabel(inef))
                .exhaustionPercent(exh).cynicismPercent(cyn).inefficacyPercent(inef)
                .totalBurnoutLevel("정상") // 필요 시 등급 로직 추가
                .build());
    }

    // AI 및 UI 로직
    private String buildStressBurnoutPrompt(String name, StressReports st, BurnoutReports bu, List<String> insights, int battery) {
        return String.format(
                "당신은 디지털 헬스케어 서비스 '심터'의 AI 심리 분석가입니다. 아래 데이터를 바탕으로 리포트를 작성하세요.\n\n" +
                        "[데이터]\n- 사용자: %s\n- 스트레스 온도: %s (PSS %d점)\n- 번아웃 상태: 소진(%s), 냉소(%s), 성취감저하(%s)\n- 마음배터리: %d%%\n- AI 정밀 통찰: %s\n\n" +
                        "[지침]\n" +
                        "1. \"%s님의 현재 스트레스 온도는 '%s' 상태입니다.\"로 시작할 것.\n" +
                        "2. AI 정밀 통찰 내용을 데이터 근거로 활용하여 사용자가 왜 힘든지 분석할 것.\n" +
                        "3. 배터리 잔량(%d%%)에 맞는 구체적인 휴식 권고와 응원 메시지를 포함할 것.\n" +
                        "4. 10문장 이내의 줄글로 작성하고 마크다운/볼드체 절대 사용 금지.",
                name, st.getStressLevel(), st.getStressScore(), bu.getExhaustionLevel(), bu.getCynicismLevel(), bu.getInefficacyLevel(),
                battery, insights.toString(), name, st.getStressLevel(), battery
        );
    }

    // AI 통찰 케이스
    private List<String> generateDetailedInsights(StressReports st, BurnoutReports bu) {
        List<String> insights = new ArrayList<>();

        // 데이터 추출 (백분율 수치 활용)
        double cp = st.getControlPercent();    // 상황 통제감
        double op = st.getOverloadPercent();   // 일상의 과부하
        double ep = bu.getExhaustionPercent(); // 정서적 소진
        double dp = bu.getCynicismPercent();   // 냉소(비인격화)
        double ip = bu.getInefficacyPercent(); // 성취감 저하

        // 지난 달 리포트 조회 및 비교
        reportsRepository.findLastReport(
                st.getUser(), "STRESS_BURNOUT_COMPLEX", st.getReport().getCreatedAt()
        ).ifPresent(prevReport -> {
            stressReportsRepository.findByReport(prevReport).ifPresent(prevSt -> {
                // 현재 통제감이 지난 리포트의 통제감보다 낮은 경우
                if (cp < prevSt.getControlPercent()) {
                    insights.add("지난 리포트보다 '통제력' 점수가 하락했습니다. 최근 갑작스러운 환경 변화가 있었는지 되돌아 보세요.");
                }
            });
        });

        // 기본 케이스 및 복합 지표 결합형 (우선순위 고려 배치)
        // Case 1: 과부하 + 소진
        if (op > 70 && ep > 70) {
            insights.add("PSS의 과부하와 번아웃의 정서적 소진이 너무 높습니다. 반복적인 업무에서 에너지가 고갈된 상태입니다.");
        }
        // 슈퍼 히어로 증후군
        if (cp > 80 && op > 80) {
            insights.add("상황을 통제하려는 의지는 매우 강하지만, 절대적인 일의 양이 한계를 넘었습니다. 책임감이 독이 되지 않도록 '거절하는 연습'이 필요합니다.");
        }
        // 심리적 고립 위험
        if (dp > 70 && ep > 70) {
            insights.add("사람들과 거리를 두려는 마음은 사실 당신이 너무 지쳤다는 신호입니다. 감정적 에너지가 바닥나서 타인을 돌볼 여유가 사라진 상태예요.");
        }
        // 무기력의 늪
        if (cp < 30 && op > 80) {
            insights.add("내가 할 수 있는 일은 없는데 해야 할 일만 쌓여가는, 가장 고통스러운 지점입니다. 지금은 문제를 해결하려 하기보다 일단 환경에서 벗어나 숨을 쉬어야 합니다.");
        }
        // 완벽주의적 소진
        if (ip > 70 && op > 70) {
            insights.add("누구보다 열심히 달리고 있지만, 스스로에 대한 기준이 너무 높아 만족을 못 하고 있습니다. 당신은 이미 충분히 잘해내고 있다는 사실을 잊지 마세요.");
        }

        // 추가 케이스 로직
        // 유능한 번아웃 (High-Achiever)
        if (ip < 20 && ep > 80) {
            insights.add("겉으로는 모든 일을 완벽히 해내고 있지만, 속은 타들어가고 있습니다. 유능함을 유지하기 위해 자신을 너무 가혹하게 채찍질하고 있지는 않나요? 성과보다 중요한 건 당신의 마음입니다.");
        }
        // 냉소적인 방어기제
        if (dp > 70 && cp > 70) {
            insights.add("상황을 통제하기 위해 의도적으로 사람들과 거리를 두고 계시네요. 타인에 대한 냉소는 사실 상처받기 싫은 당신의 마음이 만든 방어막일 수 있습니다.");
        }
        // 무색무취의 무기력
        if (op < 30 && ip > 80) {
            insights.add("일상의 압박은 적지만 삶의 의미를 찾기 어려운 상태입니다. 스트레스가 없는 것이 꼭 행복은 아닙니다. 내가 무엇을 할 때 즐거운지, 아주 작은 흥미부터 다시 찾아야 합니다.");
        }
        // 압력밥솥 상태
        if (op > 90 && cp > 50) {
            insights.add("책임감 하나로 한계까지 버티고 계시네요. 지금은 '내가 아니면 안 된다'는 생각보다, 압력이 터지기 전에 김을 조금 빼주는 '멈춤'이 절실히 필요한 시점입니다.");
        }
        // Case 3: 성취감 저하 (단독 지표)
        if (ip > 80) {
            insights.add("스스로에 대한 확신이 줄어든 상태입니다. 작은 성취부터 다시 쌓아가는 과정이 필요해 보입니다.");
        }

        // 데이터 비교 케이스
        // if (현재 통제감 < 지난 달 통제감) 로직은 추후 히스토리 기능 구현 시 적용 가능

        // 상위 3개만 리스트로 반환 (화면 노출 권장 사항)
        return insights.stream().limit(3).collect(Collectors.toList());
    }

    // 헬퍼 및 유틸리티 메서드
    private int calculateHeartBattery(int s, int b) {
        // 공식: 100 - [(PSS총점 + 번아웃총점) / 150 * 100]
        double battery = 100 - (((s + b) / 150.0) * 100);
        return (int) Math.round(Math.max(0, battery));
    }

    private String getBatteryColor(int b) {
        if (b <= 25) return "#F4574F";
        if (b <= 50) return "#FFAC79";
        if (b <= 75) return "#FAD000";
        return "#63B19B";
    }

    private String getBatteryGuide(int b) {
        if (b <= 25) return "지금 당장 휴식을 취해주세요.";
        if (b <= 50) return "에너지가 많이 고갈되었습니다. 주의가 필요해요.";
        if (b <= 75) return "조금씩 지쳐가고 있어요. 나를 돌봐주세요.";
        return "마음 에너지가 충분합니다. 아주 좋아요!";
    }

    private double calculatePercent(List<DiagnoseReqDTO.AnswerDTO> answers, List<Integer> nos, int max) {
        double sum = answers.stream().filter(a -> nos.contains(a.questionNo().intValue()))
                .mapToDouble(a -> a.score().doubleValue()).sum();
        return (sum / max) * 100;
    }

    private String mapToLabel(double p) {
        if (p >= 80) return "매우 높음";
        if (p >= 60) return "높음";
        if (p >= 40) return "보통";
        return "낮음";
    }

    private String calculateStressLevel(int score) {
        if (score >= 19) return "매우 위험";
        if (score >= 17) return "위험";
        if (score >= 14) return "경계";
        return "정상";
    }
}