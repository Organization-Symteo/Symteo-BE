package com.symteo.domain.counsel.dto.res;

import lombok.Builder;

public class CounselResDTO {

    // Converter DTO 생성을 위한 Builder
    @Builder
    public record askAiDTO(
            String text
    ){}
}
