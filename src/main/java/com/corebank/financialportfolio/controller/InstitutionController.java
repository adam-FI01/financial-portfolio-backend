package com.corebank.financialportfolio.controller;

import com.corebank.financialportfolio.dto.InstitutionResponse;
import com.corebank.financialportfolio.service.InstitutionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    public List<InstitutionResponse> list(Authentication authentication) {
        return institutionService.listForCurrentUser(authentication);
    }

}
