package com.corebank.financialportfolio.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InstitutionResponse(
        UUID id,
        String plaidInstitutionId,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
