package com.example.commerceplus.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginMemberRequest(
        @NotNull @NotBlank
        String email,
        @NotNull @NotBlank
        String password) {
}
