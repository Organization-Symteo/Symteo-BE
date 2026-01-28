package com.symteo.domain.report.controller;

import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.repository.DiagnoseRepository;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.service.DepressionAnxietyReportsService;
import com.symteo.domain.report.service.StressReportsService;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportsController {

    private final DepressionAnxietyReportsService depressionAnxietyReportsService;
    private final DiagnoseRepository diagnoseRepository;
    private final StressReportsService stressReportsService;

    // 우울/불안 리포트 생성
    @PostMapping("/depression-anxiety/{diagnoseId}")
    public ApiResponse<ReportsResponse.CreateReportResult> createReport(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        Diagnose diagnose = diagnoseRepository.findById(diagnoseId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._DIAGNOSE_NOT_FOUND));

        return ApiResponse.onSuccess(depressionAnxietyReportsService.analyzeAndSave(diagnose, userId));
    }

    // 우울/불안 리포트 조회
    @GetMapping("/depression-anxiety/{reportId}")
    public ApiResponse<ReportsResponse.DepressionAnxietyReportDetail> getReportDetail(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(depressionAnxietyReportsService.getReportDetail(reportId, userId));
    }

    // 스트레스/번아웃 리포트 생성
    @PostMapping("/stress-burnout/{diagnoseId}")
    public ApiResponse<ReportsResponse.CreateReportResult> createStressBurnout(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        Diagnose diagnose = diagnoseRepository.findById(diagnoseId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._DIAGNOSE_NOT_FOUND));

        return ApiResponse.onSuccess(stressReportsService.analyzeAndSave(diagnose, userId));
    }

    // 스트레스/번아웃 리포트 조회
    @GetMapping("/stress-burnout/{reportId}")
    public ApiResponse<ReportsResponse.IntegratedReportDetail> getStressBurnoutDetail(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(stressReportsService.getReportDetail(reportId, userId));
    }
}