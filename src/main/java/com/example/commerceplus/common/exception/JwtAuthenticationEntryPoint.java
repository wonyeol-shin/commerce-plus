package com.example.commerceplus.common.exception;

import com.example.commerceplus.common.api.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

// filter에서 jwt 검증 실패 시 사용
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException
    {
        // 응답 헤더
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // JwtFilter에서 저장한 에러 코드
        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        // 만약 아무것도 없다면 기본값으로 인증 필요 에러 설정
        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        // 동적으로 결정된 에러 코드로 ApiResponse 생성
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode);

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);

    }
}
