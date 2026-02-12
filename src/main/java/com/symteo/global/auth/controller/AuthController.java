package com.symteo.global.auth.controller;

import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.auth.dto.*;
import com.symteo.global.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/*import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;*/

/*import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;*/

@Slf4j
@RestController
    @RequestMapping("/api/v1/auth")
    @RequiredArgsConstructor
    public class AuthController {
        private final AuthService authService;

/*
        // 1. 소셜 로그인
        @GetMapping("/login/oauth2/code/{provider}")
        public void callback(
                @Parameter(hidden = true) @RequestHeader(value = "User-Agent", required = false) String userAgent,
                @PathVariable String provider,
                @RequestParam("code") String code,
                @RequestParam(value = "state", required = false) String state,
                @RequestParam(required = false) String platform,
                HttpServletRequest request,
                HttpServletResponse response
        ) throws IOException {

            AuthResponse auth = authService.login(provider, code);

            String appTargetUrl = "symteo-auth://oauth"
                    + "?accessToken=" + URLEncoder.encode(auth.accessToken(), StandardCharsets.UTF_8)
                    + "&refreshToken=" + URLEncoder.encode(auth.refreshToken(), StandardCharsets.UTF_8)
                    + "&registered=" + auth.isRegistered();

            boolean isIos = userAgent != null && (userAgent.contains("iPhone") || userAgent.contains("iPad"));

            log.info("[AUTH][REDIRECT] isIos={}, appTargetUrlPrefix={}", isIos, "symteo-auth://oauth?...");

            if (isIos) {
                // HTML 방식: iOS 사파리 및 인앱 브라우저 대응
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter writer = response.getWriter();
                writer.println("<html><body>");
                writer.println("<script type='text/javascript'>");
                writer.println("  window.location.href = '" + appTargetUrl + "';");
                writer.println("</script>");
                writer.println("<div style='text-align:center; margin-top:50px;'>");
                writer.println("  <p>앱으로 이동 중입니다...</p>");
                writer.println("  <a href='" + appTargetUrl + "' style='color: #007AFF;'>자동으로 이동하지 않는다면 여기를 클릭하세요.</a>");
                writer.println("</div>");
                writer.println("</body></html>");
                writer.flush();
            }else{
                // 302 리다이렉트 방식
                response.sendRedirect(appTargetUrl);
            }
        }
*/

    // 1. 소셜 로그인 (가입/로그인 통합)
    @PostMapping("/login/{provider}")
    public ApiResponse<AuthResponse> login(
        @PathVariable String provider,
        @RequestBody LoginRequest request // { "token": "소셜_액세스_토큰" }
    ) {
        AuthResponse response = authService.login(provider, request.getAccessToken());
        return ApiResponse.onSuccess(response);
    }

    // 2. 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.reissue(request.getRefreshToken());
        return ApiResponse.onSuccess(response);
    }

    // 3. 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.onSuccess("로그아웃이 완료되었습니다.");
    }

    // 4. 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<String> withdraw(@RequestBody WithdrawRequest request) {
        authService.withdraw(request.getUserId());
        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }

}
