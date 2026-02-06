package com.symteo.global.config;

import com.symteo.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (API 서버이므로 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Form 로그인 & HttpBasic 비활성화 (JWT 사용하므로)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 3. 세션 사용 안 함 (Stateless 설정)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // (1) Swagger 및 인증 관련 API는 모두 허용
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/api/v1/auth/**", "/error", "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(
                                //카카오, 구글, 네이버 OAuth2 리다이렉트(콜백) 주소 허용
                                "/oauth/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // DevAuthController 관련 내용이므로 이후 삭제 에정(개발용 로그인 경로는 프리패스 허용)
                        .requestMatchers("/api/v1/dev/**").permitAll()

                        // (2) 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //@Bean
    //public BCryptPasswordEncoder bCryptPasswordEncoder(){
    //   return new BCryptPasswordEncoder();
    //}
}

