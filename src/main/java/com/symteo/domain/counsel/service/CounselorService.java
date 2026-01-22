package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselorSettingReqDTO;
import com.symteo.domain.counsel.entity.CounselorSettings;
import com.symteo.domain.counsel.repository.CounselorSettingRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CounselorService {

    private final CounselorSettingRepository counselorSettingRepository;
    private final UserRepository userRepository;

    public void saveSettings(CounselorSettingReqDTO request) {

        // 1. 이미 설정한 적이 있는지 검사
        if (counselorSettingRepository.existsById(request.getUserId())) {
            throw new GeneralException(ErrorStatus.COUNSELOR_ALREADY_EXISTS);
        }

        // 2.유저 객체 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND)); // 유저가 없을 경우 에러 처리

        // 3. 엔티티 생성
        CounselorSettings newSettings = CounselorSettings.builder()
                .user(user)
                .atmosphere(request.getAtmosphere())
                .supportStyle(request.getSupportStyle())
                .roleCounselor(request.getRoleCounselor())
                .answerFormat(request.getAnswerFormat())
                .build();

        // 4. DB에 저장
        counselorSettingRepository.save(newSettings);
    }
}
