package com.seniorglez.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataPriceRepository extends JpaRepository<PriceJpaEntity, Long> {

    @Query("""
            SELECT p FROM PriceJpaEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND p.startDate <= :at
              AND p.endDate >= :at
            """)
    List<PriceJpaEntity> findCandidates(
            @Param("brandId") long brandId,
            @Param("productId") long productId,
            @Param("at") LocalDateTime at);
}
