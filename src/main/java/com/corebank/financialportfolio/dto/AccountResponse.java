package com.corebank.financialportfolio.dto;

import com.corebank.financialportfolio.entity.AccountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID institutionId,
        String plaidAccountId,
        String name,
        AccountType type,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        BigDecimal creditLimit,
        String currency,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
