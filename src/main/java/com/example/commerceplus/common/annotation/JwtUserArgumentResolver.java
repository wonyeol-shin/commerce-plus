package com.example.commerceplus.common.annotation;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.common.jwt.JwtUser;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j(topic = "argumentResolver")
@Component
public class JwtUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        boolean isJwtUserType = parameter.getParameterType().equals(JwtUser.class);

        // Auth 어노테이션 사용하는데 JwtUser 타입이 아님
        if (hasAuthAnnotation != isJwtUserType) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        return hasAuthAnnotation;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if(! (authentication.getPrincipal() instanceof JwtUser)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return (JwtUser) authentication.getPrincipal();
    }
}
