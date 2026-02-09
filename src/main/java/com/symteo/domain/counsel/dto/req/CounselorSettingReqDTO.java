package com.symteo.domain.counsel.dto.req;

import com.symteo.domain.counsel.enums.*;
import jakarta.validation.constraints.NotNull;


public class CounselorSettingReqDTO {

    public record CounselorSetting(
            @NotNull(message = "상담 분위기는 필수입니다.") Atmosphere atmosphere,
            @NotNull(message = "지지 스타일은 필수입니다.") Support_Style supportStyle,
            @NotNull(message = "상담사 역할은 필수입니다.") Counselor_Role roleCounselor,
            @NotNull(message = "답변 형식은 필수입니다.") Answer_Format answerFormat,
            @NotNull(message = "말투는 필수입니다.") Tone tone
    ){}

}
