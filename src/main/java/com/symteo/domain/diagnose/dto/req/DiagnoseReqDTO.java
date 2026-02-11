package com.symteo.domain.diagnose.dto.req;

import com.symteo.domain.diagnose.enums.DiagnoseType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class DiagnoseReqDTO {

    // 프론트엔드로부터 받는 검사 DTO
    public record DiagnoseDTO(
            @NotNull(message = "테스트 타입을 적어주세요.") DiagnoseType testType,
            @NotEmpty(message = "리스트가 비어있을 수 없습니다.")List<AnswerDTO> answers
    ){}

    // 질문 - 답과 매칭되는 DTO
    public record AnswerDTO(
            @NotNull(message = "문제 번호 형식을 작성해주세요.") Long questionNo,
            @NotNull(message = "점수가 비어있습니다.") @Min(0) @Max(5) Long score
    ){}
}
