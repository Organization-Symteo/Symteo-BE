package com.symteo.domain.diagnose.service;

import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;

import java.util.List;

public interface DiagnoseQueryService {
    public List<DiagnoseResDTO.ResultDTO> getAllDiagnose(Long userId);
    public DiagnoseResDTO.ResultDTO getDiagnose(Long userId, Long testId);
}
