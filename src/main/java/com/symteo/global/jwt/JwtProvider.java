package com.symteo.global.jwt;

import com.symteo.domain.user.enums.Role;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final long ACCESS_TOKEN_VALID_TIME = 30 * 60 * 1000L; // 30분
    private final long REFRESH_TOKEN_VALID_TIME = 14 * 24 * 60 * 60 * 1000L; // 14일

    @PostConstruct
    protected void init() {
        // 0.9.1 버전용 비밀키 생성 로직
        // secretKey를 Base64로 인코딩한 뒤 바이트 배열로 변환
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        // 알고리즘(HmacSHA256)에 맞는 키 객체 생성
        this.key = new SecretKeySpec(encodedKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    // Access Token 생성
    public String createAccessToken(Long userId, Role role) {
        return createToken(userId, role, ACCESS_TOKEN_VALID_TIME);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, null, REFRESH_TOKEN_VALID_TIME);
    }

    private String createToken(Long userId, Role role, long validTime) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        if (role != null) {
            claims.put("role", role.name());
        }

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validTime))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 토큰에서 UserId 추출
    public Long getUserId(String token) {
        String userId = Jwts.parser().setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(userId);
    }
}