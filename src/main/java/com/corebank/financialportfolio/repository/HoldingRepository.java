package com.corebank.financialportfolio.repository;

import com.corebank.financialportfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {

    void deleteByAccountId(UUID accountId);

}
