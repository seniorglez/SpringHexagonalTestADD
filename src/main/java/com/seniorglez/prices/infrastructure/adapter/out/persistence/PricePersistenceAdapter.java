package com.seniorglez.prices.infrastructure.adapter.out.persistence;

import com.seniorglez.prices.application.port.out.LoadPricesPort;
import com.seniorglez.prices.domain.model.Price;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PricePersistenceAdapter implements LoadPricesPort {

    private final SpringDataPriceRepository repository;
    private final PriceEntityMapper mapper;

    public PricePersistenceAdapter(SpringDataPriceRepository repository, PriceEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Price> loadCandidates(long brandId, long productId, LocalDateTime at) {
        return repository.findCandidates(brandId, productId, at).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
