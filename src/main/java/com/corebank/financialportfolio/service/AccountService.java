package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.dto.AccountResponse;
import com.corebank.financialportfolio.entity.Account;
import com.corebank.financialportfolio.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<AccountResponse> listForCurrentUser(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return accountRepository.findByInstitutionUserId(user.getId()).stream()
                .map(AccountService::toResponse)
                .toList();
    }

    private static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getInstitution().getId(),
                account.getPlaidAccountId(),
                account.getName(),
                account.getType(),
                account.getCurrentBalance(),
                account.getAvailableBalance(),
                account.getCreditLimit(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

}
