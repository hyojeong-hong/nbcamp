package com.hobbyhop.domain.user.dto;

import com.hobbyhop.domain.user.dto.validCustom.NullablePattern;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String info;

    @NotBlank(message = "oldPassword : 프로필 수정 시 필수 입력 값입니다.")
    private String oldPassword;

    @Size(min=8 , max=15)
    private String newPassword;

    private String confirmPassword;
}
