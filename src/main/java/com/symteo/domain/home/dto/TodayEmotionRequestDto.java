package com.symteo.domain.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodayEmotionRequestDto {
    @NotNull(message = "날씨 값은 필수입니다.")
    @Min(value = 1, message = "날씨 값은 1에서 4 사이여야 합니다.")
    @Max(value = 4, message = "날씨 값은 1에서 4 사이여야 합니다.")
    private Integer weather;
}