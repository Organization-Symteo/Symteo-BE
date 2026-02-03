package com.symteo.domain.home.service;

import com.symteo.domain.home.entity.TodayLines;
import com.symteo.domain.home.repository.TodayLineRepository;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TodayLineService {

    private final TodayLineRepository todayLineRepository;
    private final UserRepository userRepository;

    // 오늘의 한 줄 조회 api
    @Transactional(readOnly = true)
    public String getTodayLine(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 전체 문구 개수 확인
        long totalCount = todayLineRepository.count();
        if (totalCount == 0) throw new GeneralException(ErrorStatus._TODAY_LINE_NOT_FOUND);

        // 오늘 날짜를 기준으로 고유 인덱스 계산
        // (오늘 날짜 숫자 % 전체 개수) 방식으로 매일 다른 인덱스를 결정
        long dayIncr = LocalDate.now().toEpochDay();
        int targetIndex = (int) (dayIncr % totalCount);

        // 해당 순서의 문구 하나만 가져오기
        Page<TodayLines> page = todayLineRepository.findAll(PageRequest.of(targetIndex, 1));

        return page.getContent().get(0).getContents();
    }
}
