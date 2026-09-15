package com.example.commerceplus.common.jwt;

import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;

public record JwtUser(
        Long id,
        String email,
        String name,
        String phoneNumber,
        MemberRole role,
        MemberStatus status) {
}
