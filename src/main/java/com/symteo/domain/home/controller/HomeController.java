package com.symteo.domain.home.controller;

import com.symteo.domain.home.service.TodayLineService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final TodayLineService todayLineService;

    @GetMapping("/today-lines")
    public ApiResponse<String> testGenerateLine() {
        return ApiResponse.onSuccess(todayLineService.getTodayLine());
    }
}