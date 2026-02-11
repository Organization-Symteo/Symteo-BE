package com.symteo.domain.home.service;

import com.symteo.domain.home.entity.TodayLines;
import com.symteo.domain.home.exception.HomeErrorCode;
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
        // 1. 공통 멤버 예외 사용
        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 2. 도메인 전용 에러 코드 적용
        long totalCount = todayLineRepository.count();
        if (totalCount == 0) throw new GeneralException(HomeErrorCode._TODAY_LINE_NOT_FOUND);

        // 3. 오늘 날짜를 기준으로 고유 인덱스 계산
        long dayIncr = LocalDate.now().toEpochDay();
        int targetIndex = (int) (dayIncr % totalCount);

        // 4. 인덱스 에러 방지 로직 추가
        Page<TodayLines> page = todayLineRepository.findAll(PageRequest.of(targetIndex, 1));

        if (page.isEmpty() || page.getContent().isEmpty()) {
            // 계산된 인덱스에 데이터가 없는 경우(데이터 삭제 등), 첫 번째 데이터를 반환하는 안전장치
            return todayLineRepository.findAll(PageRequest.of(0, 1))
                    .getContent().get(0).getContents();
        }

        return page.getContent().get(0).getContents();
    }
}