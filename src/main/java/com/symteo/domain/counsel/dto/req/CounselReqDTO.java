package com.symteo.domain.counsel.dto.req;

public class CounselReqDTO {

    public record ChatMessage(
            Long userId,
            String text
    ){}
}
