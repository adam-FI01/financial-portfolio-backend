package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.dto.InstitutionResponse;
import com.corebank.financialportfolio.entity.Institution;
import com.corebank.financialportfolio.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<InstitutionResponse> listForCurrentUser(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return institutionRepository.findByUserId(user.getId()).stream()
                .map(InstitutionService::toResponse)
                .toList();
    }

    private static InstitutionResponse toResponse(Institution institution) {
        return new InstitutionResponse(
                institution.getId(),
                institution.getPlaidInstitutionId(),
                institution.getName(),
                institution.getCreatedAt(),
                institution.getUpdatedAt()
        );
    }

}
