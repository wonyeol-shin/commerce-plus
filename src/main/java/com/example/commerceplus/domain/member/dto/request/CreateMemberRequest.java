package com.example.commerceplus.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateMemberRequest(

        @NotNull @NotBlank
        String email,
        @NotNull @NotBlank
        String name,
        @NotNull @NotBlank
        String password,
        @NotNull @NotBlank
        String checkPassword,
        @NotNull @NotBlank @Pattern(regexp = "^01([0|1|6|7|8|9])-([0-9]{3,4})-([0-9]{4})$")
        String phoneNumber
)
{
    public boolean isPasswordCorrect() {
        return password.equals(checkPassword);
    }
}
