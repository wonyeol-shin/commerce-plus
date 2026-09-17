package com.example.commerceplus.domain.member.sevice;

import com.example.commerceplus.common.bean.PasswordEncoder;
import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.common.jwt.JwtUtil;
import com.example.commerceplus.domain.member.dto.request.CreateAdminRequest;
import com.example.commerceplus.domain.member.dto.request.CreateMemberRequest;
import com.example.commerceplus.domain.member.dto.request.LoginMemberRequest;
import com.example.commerceplus.domain.member.dto.response.CreateAdminResponse;
import com.example.commerceplus.domain.member.dto.response.CreateMemberResponse;
import com.example.commerceplus.domain.member.dto.response.GetAllAdminResponse;
import com.example.commerceplus.domain.member.dto.response.LoginMemberResponse;
import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;
import com.example.commerceplus.domain.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public CreateMemberResponse createMember(CreateMemberRequest request) {

        // 생성패스워드와 검증패스워드가 다름
        if (!request.isPasswordCorrect()) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // 이미 존재하는 이메일
        boolean findMember = memberRepository.existsByEmail(request.email());
        if (findMember) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.createNormalMember(request.email(),encodedPassword, request.name(), request.phoneNumber());
        Member savedMember = memberRepository.save(member);

        return CreateMemberResponse.from(savedMember);
    }

    public CreateAdminResponse createAdmin(CreateAdminRequest request) {

        if (request.role() == MemberRole.NORMAL) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_ROLE_EXCEPTION);
        }

        // 생성패스워드와 검증패스워드가 다름
        if (!request.isPasswordCorrect()) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        // 이미 존재하는 이메일
        boolean findMember = memberRepository.existsByEmail(request.email());
        if (findMember) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.createAdminMember(request.email(), encodedPassword, request.name(), request.phoneNumber(), request.role());
        Member savedMember = memberRepository.save(member);

        return CreateAdminResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public LoginMemberResponse loginMember(@Valid LoginMemberRequest request) {

       Optional<Member> findMember = memberRepository.findByEmail(request.email());

       if (findMember.isEmpty()) {
           throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
       }

       Member member = findMember.get();

       boolean matches = passwordEncoder.matches(request.password(), member.getPassword());

       if (!matches) {
           throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
       }

        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_ACCOUNT);
        }

       String token = jwtUtil.createToken(
               member.getId(),
               member.getEmail(),
               member.getName(),
               member.getPhoneNumber(),
               member.getRole(),
               member.getStatus());

       return new LoginMemberResponse(token);
    }

    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<GetAllAdminResponse> findAllAdmin() {

        List<MemberRole> roleList = Arrays.stream(MemberRole.values())
                .filter(role -> role != MemberRole.NORMAL) // NORMAL이 아닌 것만 (ex: ADMIN, MANAGER 등)
                .toList();

        List<Member> members = memberRepository.findByRoles(roleList);
        return members.stream().map(GetAllAdminResponse::from).toList();
    }
}
