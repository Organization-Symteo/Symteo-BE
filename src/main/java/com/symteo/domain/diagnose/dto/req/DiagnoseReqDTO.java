package com.symteo.domain.diagnose.dto.req;

import com.symteo.domain.diagnose.enums.DiagnoseType;

import java.util.List;

public class DiagnoseReqDTO {

    // 프론트엔드로부터 받는 검사 DTO
    public record DiagnoseDTO(
            DiagnoseType testType,
            List<AnswerDTO> answers
    ){}

    // 질문 - 답과 매칭되는 DTO
    public record AnswerDTO(
            Long questionNo,
            Long score
    ){}
}
