package com.seniorglez.prices.infrastructure.adapter.out.persistence;

import com.seniorglez.prices.domain.model.Price;
import org.springframework.stereotype.Component;

@Component
public class PriceEntityMapper {

    Price toDomain(PriceJpaEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceList(),
                entity.getPriority(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getAmount(),
                entity.getCurrency());
    }
}
