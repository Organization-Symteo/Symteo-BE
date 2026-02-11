package com.symteo.domain.diagnose.service;

import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
import com.symteo.domain.diagnose.converter.DiagnoseConverter;
import com.symteo.domain.diagnose.dto.res.DiagnoseResDTO;
import com.symteo.domain.diagnose.entity.Diagnose;
import com.symteo.domain.diagnose.exception.DiagnoseErrorCode;
import com.symteo.domain.diagnose.exception.DiagnoseException;
import com.symteo.domain.diagnose.repository.DiagnoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiagnoseQueryServiceImpl implements DiagnoseQueryService{

    private final DiagnoseRepository diagnoseRepository;

    // 진단 전체 조회
    @Override
    public List<DiagnoseResDTO.ResultDTO> getAllDiagnose(Long userId) {

        List<Diagnose> allByUserId = diagnoseRepository.findAllByUserId(userId)
                .orElseThrow(() -> new DiagnoseException(DiagnoseErrorCode._DIAGNOSE_NOT_FOUND));

        List<DiagnoseResDTO.ResultDTO> allDiagnoses = allByUserId.stream()
                .map(DiagnoseConverter::toResultDTO)
                .toList();

        return allDiagnoses;
    }

    // 진단 단일 조회
    @Override
    public DiagnoseResDTO.ResultDTO getDiagnose(Long userId, Long testId) {

        Diagnose diagnose = diagnoseRepository.findById(testId)
                .orElseThrow(() -> new DiagnoseException(DiagnoseErrorCode._DIAGNOSE_NOT_FOUND));

        if(!Objects.equals(diagnose.getUserId(), userId)){ // null 안전 eqauls
            throw new DiagnoseException(DiagnoseErrorCode._DIAGNOSE_ACCESS_DENIED);
        }

        return DiagnoseConverter.toResultDTO(diagnose);
    }
}
