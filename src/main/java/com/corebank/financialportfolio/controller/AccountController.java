package com.corebank.financialportfolio.controller;

import com.corebank.financialportfolio.dto.AccountResponse;
import com.corebank.financialportfolio.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> list(Authentication authentication) {
        return accountService.listForCurrentUser(authentication);
    }

}
