package com.symteo.domain.diagnose.service;

import com.symteo.domain.diagnose.converter.DiagnoseConverter;
import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;
import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.exception.DiagnoseErrorCode;
import com.symteo.domain.diagnose.exception.DiagnoseException;
import com.symteo.domain.diagnose.repository.DiagnoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiagnoseCommandServiceImpl implements DiagnoseCommandService{

    private final DiagnoseRepository diagnoseRepository;

    @Override
    public DiagnoseResDTO.CreateDTO createDiagnose(Long userId, DiagnoseReqDTO.DiagnoseDTO dto) {

        Diagnose savedDiagnose = diagnoseRepository.save(DiagnoseConverter.toDiagnose(userId, dto));
        return new DiagnoseResDTO.CreateDTO(savedDiagnose.getId());
    }

    @Override
    public DiagnoseResDTO.DeleteDTO deleteDiagnose(Long userId, Long diagnoseId) {

        Diagnose diagnose = diagnoseRepository.findById(diagnoseId)
                .orElseThrow(() -> new DiagnoseException(DiagnoseErrorCode._DIAGNOSE_NOT_FOUND));

        if(!Objects.equals(diagnose.getUserId(), userId)){ // null 안전 eqauls
            throw new DiagnoseException(DiagnoseErrorCode._DIAGNOSE_ACCESS_DENIED);
        }

        diagnoseRepository.delete(diagnose);
        return new DiagnoseResDTO.DeleteDTO(diagnose.getId());
    }
}
