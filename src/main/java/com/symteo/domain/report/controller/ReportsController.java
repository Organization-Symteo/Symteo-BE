package com.symteo.domain.report.controller;

import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.repository.DiagnoseRepository;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.service.DiagnoseReportService;
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

    private final DiagnoseReportService diagnoseReportService;
    private final DiagnoseRepository diagnoseRepository;

    @PostMapping("/depression-anxiety/{diagnoseId}")
    public ApiResponse<ReportsResponse.CreateReportResult> createReport(
            @PathVariable Long diagnoseId,
            @AuthenticationPrincipal Long userId
    ) {
        Diagnose diagnose = diagnoseRepository.findById(diagnoseId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._DIAGNOSE_NOT_FOUND));

        return ApiResponse.onSuccess(diagnoseReportService.analyzeAndSave(diagnose, userId));
    }

    @GetMapping("/depression-anxiety/{reportId}")
    public ApiResponse<ReportsResponse.ReportDetail> getReportDetail(
            @PathVariable Long reportId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(diagnoseReportService.getReportDetail(reportId, userId));
    }
}