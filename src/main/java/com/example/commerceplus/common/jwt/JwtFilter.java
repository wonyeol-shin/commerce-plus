package com.example.commerceplus.common.jwt;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j(topic = "JwtFilter")
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isEmpty() ) {
            request.setAttribute("exception", ErrorCode.UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        String token;

        try {
            token = jwtUtil.substringToken(authorization);
        } catch (BusinessException e) {
            request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
            filterChain.doFilter(request, response);
            return;
        }

        Long id = jwtUtil.getUserId(token);
        String email = jwtUtil.extractUserEmail(token);
        String name = jwtUtil.extractUserName(token);
        String phoneNumber = jwtUtil.extractUserPhone(token);
        MemberRole role = MemberRole.valueOf(jwtUtil.extractRole(token));
        MemberStatus status = MemberStatus.valueOf(jwtUtil.extractStatus(token));
        JwtUser jwtUser = new JwtUser(id, email, name, phoneNumber, role, status);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(
                "ROLE_" + role.name());

        Collection<? extends GrantedAuthority> authorities = List.of(grantedAuthority);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        jwtUser, null,authorities
                )
        );

        filterChain.doFilter(request, response);

    }
}
