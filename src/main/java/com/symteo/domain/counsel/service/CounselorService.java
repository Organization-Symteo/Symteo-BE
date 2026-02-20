package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselorSettingReqDTO;
import com.symteo.domain.counsel.entity.CounselorSettings;
import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
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

    public Long saveSettings(Long userId, CounselorSettingReqDTO.CounselorSetting request) {
        

        // 2.유저 객체 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND)); // 유저가 없을 경우 에러 처리

        // 3. 엔티티 생성
        CounselorSettings newSettings = CounselorSettings.builder()
                .user(user)
                .atmosphere(request.atmosphere())
                .supportStyle(request.supportStyle())
                .roleCounselor(request.roleCounselor())
                .answerFormat(request.answerFormat())
                .tone(request.tone())
                .build();

        // 4. DB에 저장
        try {
            counselorSettingRepository.save(newSettings);
        } catch (Exception e) {
            throw new CounselException(CounselErrorCode._SETTING_SAVE_ERROR);
        }

        return userId;
    }
}
