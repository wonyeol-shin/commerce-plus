package com.example.commerceplus.domain.member.controller;

import com.example.commerceplus.common.annotation.Auth;
import com.example.commerceplus.common.api.ApiResponse;
import com.example.commerceplus.common.jwt.JwtUser;
import com.example.commerceplus.domain.member.dto.response.GetAllAdminResponse;
import com.example.commerceplus.domain.member.sevice.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AdminController {

    private final MemberService memberService;

    @PreAuthorize(" authentication.principal.status.name() == 'ACTIVE' and hasRole('ADMIN') ")
    @GetMapping("/api/admins")
    public ResponseEntity<ApiResponse<List<GetAllAdminResponse>>> getAdminAll(){
        return ResponseEntity.ok(ApiResponse.ok(memberService.findAllAdmin()));
    }
}
