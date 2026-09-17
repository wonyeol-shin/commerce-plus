package com.example.commerceplus.domain.member.dto.response;

import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;

public record CreateAdminResponse (Long id, String email, String name, String phoneNumber, MemberRole role, MemberStatus status)
{
    public static CreateAdminResponse from(Member member) {
        return new CreateAdminResponse(member.getId(),  member.getEmail(), member.getName(), member.getPhoneNumber(), member.getRole(), member.getStatus());
    }
}
