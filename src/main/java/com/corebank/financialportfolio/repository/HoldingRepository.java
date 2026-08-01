package com.corebank.financialportfolio.repository;

import com.corebank.financialportfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {

    @Query("""
            SELECT h FROM Holding h
            JOIN FETCH h.account a
            JOIN FETCH a.institution i
            WHERE i.user.id = :userId
            """)
    List<Holding> findByAccountInstitutionUserId(@Param("userId") UUID userId);

    void deleteByAccountId(UUID accountId);

}
