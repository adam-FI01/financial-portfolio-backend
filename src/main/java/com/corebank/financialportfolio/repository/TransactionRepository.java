package com.corebank.financialportfolio.repository;

import com.corebank.financialportfolio.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccountInstitutionUserId(UUID userId, Pageable pageable);

    void deleteByAccountId(UUID accountId);

}
