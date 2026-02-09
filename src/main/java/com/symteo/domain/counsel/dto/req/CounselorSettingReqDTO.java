package com.symteo.domain.counsel.dto.req;

import com.symteo.domain.counsel.enums.*;

import javax.validation.constraints.NotNull;

public class CounselorSettingReqDTO {

    public record CounselorSetting(
            @NotNull Atmosphere atmosphere,
            @NotNull Support_Style supportStyle,
            @NotNull Counselor_Role roleCounselor,
            @NotNull Answer_Format answerFormat,
            @NotNull Tone tone
    ){}

}
