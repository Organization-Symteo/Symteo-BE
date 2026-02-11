package com.symteo.domain.report.controller;

import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.service.AttachmentReportsService;
import com.symteo.domain.report.service.DepressionAnxietyReportsService;
import com.symteo.domain.report.service.StressReportsService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportsController {

    private final DepressionAnxietyReportsService depressionAnxietyReportsService;
    private final StressReportsService stressReportsService;
    private final AttachmentReportsService attachmentReportsService;

    // 우울/불안 리포트 생성
    @PostMapping("/diagnose/{diagnoseId}/depression-anxiety")
    public ApiResponse<ReportsResponse.CreateReportResult> createReport(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(depressionAnxietyReportsService.analyzeAndSave(diagnoseId, userId));
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
    @PostMapping("/diagnose/{diagnoseId}/stress-burnout")
    public ApiResponse<ReportsResponse.CreateReportResult> createStressBurnout(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(stressReportsService.analyzeAndSave(diagnoseId, userId));
    }

    // 스트레스/번아웃 리포트 조회
    @GetMapping("/stress-burnout/{reportId}")
    public ApiResponse<ReportsResponse.IntegratedReportDetail> getStressBurnoutDetail(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(stressReportsService.getReportDetail(reportId, userId));
    }

    // 성향 리포트 생성
    @PostMapping("/diagnose/{diagnoseId}/attachment")
    public ApiResponse<ReportsResponse.CreateReportResult> createAttachment(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(attachmentReportsService.analyzeAndSave(diagnoseId, userId));
    }

    // 성향 리포트 조회
    @GetMapping("/attachment/{reportId}")
    public ApiResponse<ReportsResponse.AttachmentReportDetail> getAttachmentDetail(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(attachmentReportsService.getReportDetail(reportId, userId));
    }
}