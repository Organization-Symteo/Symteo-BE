package com.symteo.domain.diagnose.converter;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;
import com.symteo.domain.diagnose.entity.Diagnose;

public class DiagnoseConverter {

    // DTO -> Entity
    // DTO를 Diagnose 엔티티로 변경
    public static Diagnose toDiagnose(
            DiagnoseReqDTO.DiagnoseDTO dto
    ){
        return Diagnose.builder()
                .userId(dto.userId())
                .testType(dto.testType())
                .answers(dto.answers())
                .build();
    }

    // Entity -> DTO
    public static DiagnoseResDTO.ResultDTO toResultDTO(
            Diagnose diagnose
    ){
        return DiagnoseResDTO.ResultDTO.builder()
                .userId(diagnose.getUserId())
                .testType(diagnose.getTestType())
                .answers(diagnose.getAnswers())
                .build();
    }

}
