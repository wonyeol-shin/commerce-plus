package com.example.commerceplus.common.jwt;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.member.entity.MemberRole;
import com.example.commerceplus.domain.member.entity.MemberStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.expiration}")
    private long tokenTime; // 60분

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken
            (Long memberId,
             String email,
             String name,
             String phoneNumber,
             MemberRole role,
             MemberStatus status)
    {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .subject(String.valueOf(memberId))
                        .claim("email", email)
                        .claim("name", name)
                        .claim("phoneNumber", phoneNumber)
                        .claim("role", role.name())
                        .claim("status", status.name())
                        .expiration(new Date(date.getTime() + tokenTime))
                        .issuedAt(date) // 발급일
                        .signWith(key)
                        .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    public String extractUserEmail(String token) {
        return extractClaims(token).get("email").toString();
    }

    public String extractUserName(String token) {return extractClaims(token).get("name").toString();}

    public String extractUserPhone(String token) {return extractClaims(token).get("phoneNumber").toString();}

    public String extractRole(String token) {
        return extractClaims(token).get("role").toString();
    }

    public String extractStatus(String token) {
        return extractClaims(token).get("status").toString();
    }

    public boolean validateToken(String token) {

        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            extractClaims(token);
            return true;
        }catch (JwtException e) {
            return false;
        }

    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }




}
