package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.dto.res.CounselResDTO;
import lombok.Builder;

public class CounselConverter {

    // 엔티티 -> DTO
    // AI 답변을 String으로 변환
    public static CounselResDTO.askAiDTO EntityToCounsel(
            String answer
    ){
        return CounselResDTO.askAiDTO.builder()
                .text(answer)
                .build();
    }
}
