package com.symteo.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String token; // 프론트에서 주는 소셜 Access Token
}
