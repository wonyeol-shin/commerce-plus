package com.example.commerceplus.common.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
public class SpaDirectResourceController {
    @GetMapping(
            value = "/{path:^(?!api|actuator|auth|portone|assets|favicon\\.svg|index\\.html).*$}/**",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> serveSpa(HttpServletRequest request) {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("SPA Index.html 파일을 찾을 수 없습니다.");
        }
    }
}
