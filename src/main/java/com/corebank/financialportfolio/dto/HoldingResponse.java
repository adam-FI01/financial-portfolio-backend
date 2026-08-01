package com.corebank.financialportfolio.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record HoldingResponse(
        UUID id,
        UUID accountId,
        String accountName,
        String institutionName,
        String symbol,
        String name,
        BigDecimal shares,
        BigDecimal costBasis,
        BigDecimal currentValue,
        BigDecimal gainLoss,
        BigDecimal gainLossPercent,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
