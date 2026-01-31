package com.symteo.domain.counsel.dto.req;

import com.symteo.domain.counsel.enums.*;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CounselorSettingReqDTO {
    private Long userId;

    private Atmosphere atmosphere;
    private Support_Style supportStyle;
    private Counselor_Role roleCounselor;
    private Answer_Format answerFormat;
    private Tone tone;
}
