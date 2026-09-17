package com.example.commerceplus.domain.member.dto.response;

import com.example.commerceplus.domain.member.entity.Member;

public record CreateMemberResponse(Long id, String email, String name, String phoneNumber)
{
    public static CreateMemberResponse from(Member member) {
        return new CreateMemberResponse(member.getId(),  member.getEmail(), member.getName(), member.getPhoneNumber());
    }
}
