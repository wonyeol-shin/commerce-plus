package com.example.commerceplus.domain.member.controller;

import com.example.commerceplus.common.api.ApiResponse;
import com.example.commerceplus.domain.member.dto.request.CreateAdminRequest;
import com.example.commerceplus.domain.member.dto.request.CreateMemberRequest;
import com.example.commerceplus.domain.member.dto.request.LoginMemberRequest;
import com.example.commerceplus.domain.member.dto.response.CreateAdminResponse;
import com.example.commerceplus.domain.member.dto.response.CreateMemberResponse;
import com.example.commerceplus.domain.member.dto.response.LoginMemberResponse;
import com.example.commerceplus.domain.member.sevice.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.createMember(request)));
    }

    @PostMapping("/admins/signup")
    public ResponseEntity<ApiResponse<CreateAdminResponse>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.createAdmin(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginMemberResponse>> loginMember(@Valid @RequestBody LoginMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.loginMember(request)));
    }

}
