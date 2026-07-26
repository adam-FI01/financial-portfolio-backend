package com.corebank.financialportfolio.repository;

import com.corebank.financialportfolio.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID> {

    List<Institution> findByUserId(UUID userId);

}
