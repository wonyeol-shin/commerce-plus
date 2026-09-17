package com.example.commerceplus.domain.payment.infra.portone;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class PortOneConfig {

    private final PortOneProperties portOneProperties;

    @Bean
    public RestClient portOneRestClient() {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(3000); // 3초 연결 실패 시 실패
        clientHttpRequestFactory.setReadTimeout(5000); // 응답이 5초동안 오지 않으면 실패

        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory)
                .baseUrl(portOneProperties.getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + portOneProperties.getApiSecret())
                .build();
    }

}
