package com.symteo.domain.user.dto;

import com.symteo.domain.counsel.enums.Answer_Format;
import com.symteo.domain.counsel.enums.Atmosphere;
import com.symteo.domain.counsel.enums.Counselor_Role;
import com.symteo.domain.counsel.enums.Support_Style;
import lombok.Builder;

@Builder
public record CounselorSettingsResponse(
    Atmosphere atmosphere,         // 분위기 (emotional, warm, calm)
    Support_Style supportStyle,     // 지지 스타일 (empathic, solution, fact)
    Counselor_Role roleCounselor,   // 상담사 역할 (counselor, friend, mental_coatch)
    Answer_Format answerFormat      // 답변 형식 (situational, short_format, long_format)
) {
    public static CounselorSettingsResponse of(
        Atmosphere atmosphere,
        Support_Style supportStyle,
        Counselor_Role roleCounselor,
        Answer_Format answerFormat
    ) {
        return CounselorSettingsResponse.builder()
                .atmosphere(atmosphere)
                .supportStyle(supportStyle)
                .roleCounselor(roleCounselor)
                .answerFormat(answerFormat)
                .build();
    }
}

