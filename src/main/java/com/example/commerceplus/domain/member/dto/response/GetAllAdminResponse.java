package com.example.commerceplus.domain.member.dto.response;

import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;
import java.time.LocalDateTime;


public record GetAllAdminResponse(
        Long id,
        String email,
        String name,
        String phoneNuer,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {

    public static GetAllAdminResponse from(Member member) {
        return new GetAllAdminResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }

}
