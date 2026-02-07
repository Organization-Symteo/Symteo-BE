package com.symteo.domain.counsel.dto.ai;

import lombok.Builder;

public class AiAnswerDTO {

    @Builder
    public record AnswerDTO(
            String content
    ){}
}
