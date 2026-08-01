package com.corebank.financialportfolio.controller;

import com.corebank.financialportfolio.dto.HoldingResponse;
import com.corebank.financialportfolio.service.HoldingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping
    public List<HoldingResponse> list(Authentication authentication) {
        return holdingService.listForCurrentUser(authentication);
    }

}
