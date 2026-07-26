package com.corebank.financialportfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        String plaidTransactionId,
        BigDecimal amount,
        String description,
        String category,
        LocalDate transactionDate,
        boolean pending,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
