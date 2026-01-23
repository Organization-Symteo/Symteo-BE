package com.symteo.domain.user.dto;

import lombok.Getter;

@Getter
public class UpdateUserSettingsRequest {
    private Boolean isCheerMsgOn;
    private Boolean isAnalysisReadyOn;
    private Boolean isMonthlyReportOn;
    private Boolean isLockEnabled;
}

