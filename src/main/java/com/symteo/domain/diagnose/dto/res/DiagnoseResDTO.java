package com.symteo.domain.diagnose.dto.res;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import lombok.Builder;

import java.util.List;

public class DiagnoseResDTO {

    @Builder
    public record CreateDTO(
            Long diagnoseId
    ){}

    @Builder
    public record ResultDTO(
            Long userId,
            String testType,
            List<DiagnoseReqDTO.AnswerDTO> answers
    ){}

    @Builder
    public record DeleteDTO(
            Long diagnoseId
    ){}
}
