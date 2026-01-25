package com.symteo.domain.diagnose.controller;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;
import com.symteo.domain.diagnose.service.DiagnoseCommandService;
import com.symteo.domain.diagnose.service.DiagnoseQueryService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diagnose")
public class DiagnoseController {

    private final DiagnoseCommandService diagnoseCommandService;
    private final DiagnoseQueryService diagnoseQueryService;

    // 검사 생성하기
    @PostMapping("/upload")
    public ApiResponse<DiagnoseResDTO.CreateDTO> askDiagnose(
            @RequestBody DiagnoseReqDTO.DiagnoseDTO answers
    ){
        return ApiResponse.onSuccess(diagnoseCommandService.createDiagnose(answers));
    }

    // 전체 검사 조회하기
    @GetMapping("/all")
    public ApiResponse<List<DiagnoseResDTO.ResultDTO>> getAllDiagnose(
            @RequestParam Long userId
    ){
        return ApiResponse.onSuccess(diagnoseQueryService.getAllDiagnose(userId));
    }

    /**
     * // 1. 전체 합산 점수
     * long totalScore = answers.stream()
     *         .mapToLong(AnswerDTO::score)
     *         .sum();
     *
     * // 2. 1~3번 문항 구간 점수 (지표 A)
     * long indicatorA = answers.stream()
     *         .filter(a -> a.questionNo() >= 1 && a.questionNo() <= 3)
     *         .mapToLong(AnswerDTO::score)
     *         .sum();
     *
     * // 3. 4~6번 문항 구간 점수 (지표 B)
     * long indicatorB = answers.stream()
     *         .filter(a -> a.questionNo() >= 4 && a.questionNo() <= 6)
     *         .mapToLong(AnswerDTO::score)
     *         .sum();
     * */

    // 단일 검사 조회하기
    @GetMapping("/{testId}")
    public ApiResponse<DiagnoseResDTO.ResultDTO> getDiagnose(
            @PathVariable Long testId
    ){
        return ApiResponse.onSuccess(diagnoseQueryService.getDiagnose(testId));
    }

    // 검사 삭제하기
    @DeleteMapping("/{testId}")
    public ApiResponse<DiagnoseResDTO.DeleteDTO> deleteDiagnose(
            @PathVariable Long testId
    ){
        return ApiResponse.onSuccess(diagnoseCommandService.deleteDiagnose(testId));
    }
}
