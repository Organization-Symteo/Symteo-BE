package com.symteo.domain.user.dto;

import lombok.Builder;

@Builder
public record NicknameCheckResponse (
        boolean isDuplicated
){

}

