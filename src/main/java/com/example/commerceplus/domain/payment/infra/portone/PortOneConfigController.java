package com.example.commerceplus.domain.payment.infra.portone;

import com.example.commerceplus.common.api.ApiResponse;
import com.example.commerceplus.domain.payment.infra.portone.dto.PortOneConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortOneConfigController {

    private final PortOneProperties portOneProperties;

    @GetMapping("/portone/config")
    public ResponseEntity<ApiResponse<PortOneConfigResponse>> getPortOneConfig() {
        return ResponseEntity.ok(ApiResponse.ok(new PortOneConfigResponse(
                portOneProperties.getStoreId(), portOneProperties.getChannelKey()
        )));
    }
}
