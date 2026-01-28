package com.symteo.domain.user.dto;

import com.symteo.domain.counsel.enums.Answer_Format;
import com.symteo.domain.counsel.enums.Atmosphere;
import com.symteo.domain.counsel.enums.Counselor_Role;
import com.symteo.domain.counsel.enums.Support_Style;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCounselorSettingsRequest {
    private Atmosphere atmosphere;
    private Support_Style supportStyle;
    private Counselor_Role roleCounselor;
    private Answer_Format answerFormat;
}

