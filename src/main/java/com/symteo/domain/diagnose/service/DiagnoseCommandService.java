package com.symteo.domain.diagnose.service;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;

public interface DiagnoseCommandService {
    public DiagnoseResDTO.CreateDTO createDiagnose(DiagnoseReqDTO.DiagnoseDTO dto);
    public DiagnoseResDTO.DeleteDTO deleteDiagnose(Long diagnoseId);
}
