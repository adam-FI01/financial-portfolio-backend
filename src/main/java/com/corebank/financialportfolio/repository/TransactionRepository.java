package com.corebank.financialportfolio.repository;

import com.corebank.financialportfolio.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.institution.user.id = :userId
            AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            AND (:category IS NULL OR t.category = :category)
            """)
    Page<Transaction> search(
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("category") String category,
            Pageable pageable);

    void deleteByAccountId(UUID accountId);

}
