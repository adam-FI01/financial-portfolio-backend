package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.dto.HoldingResponse;
import com.corebank.financialportfolio.entity.Holding;
import com.corebank.financialportfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<HoldingResponse> listForCurrentUser(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return holdingRepository.findByAccountInstitutionUserId(user.getId()).stream()
                .map(HoldingService::toResponse)
                .toList();
    }

    private static HoldingResponse toResponse(Holding holding) {
        BigDecimal gainLoss = holding.getCurrentValue().subtract(holding.getCostBasis());
        BigDecimal gainLossPercent = holding.getCostBasis().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gainLoss.divide(holding.getCostBasis(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new HoldingResponse(
                holding.getId(),
                holding.getAccount().getId(),
                holding.getAccount().getName(),
                holding.getAccount().getInstitution().getName(),
                holding.getSymbol(),
                holding.getName(),
                holding.getShares(),
                holding.getCostBasis(),
                holding.getCurrentValue(),
                gainLoss,
                gainLossPercent,
                holding.getCreatedAt(),
                holding.getUpdatedAt()
        );
    }

}
