package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.dto.PageResponse;
import com.corebank.financialportfolio.dto.TransactionResponse;
import com.corebank.financialportfolio.entity.Transaction;
import com.corebank.financialportfolio.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listForCurrentUser(
            Authentication authentication, int page, int size, String search, String category) {
        var user = currentUserService.getCurrentUser(authentication);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        var result = transactionRepository.search(user.getId(), search, category, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(TransactionService::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getPlaidTransactionId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getTransactionDate(),
                transaction.isPending(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

}
