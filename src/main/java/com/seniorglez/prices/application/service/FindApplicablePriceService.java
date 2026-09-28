package com.seniorglez.prices.application.service;

import com.seniorglez.prices.application.port.in.FindApplicablePriceQuery;
import com.seniorglez.prices.application.port.in.FindApplicablePriceUseCase;
import com.seniorglez.prices.application.port.out.LoadPricesPort;
import com.seniorglez.prices.domain.exception.PriceNotFoundException;
import com.seniorglez.prices.domain.model.Price;
import com.seniorglez.prices.domain.service.PriceSelector;

import java.util.List;

public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private final LoadPricesPort loadPricesPort;
    private final PriceSelector priceSelector;

    public FindApplicablePriceService(LoadPricesPort loadPricesPort, PriceSelector priceSelector) {
        this.loadPricesPort = loadPricesPort;
        this.priceSelector = priceSelector;
    }

    @Override
    public Price find(FindApplicablePriceQuery query) {
        List<Price> candidates = loadPricesPort.loadCandidates(
                query.brandId(), query.productId(), query.applicationDate());

        return priceSelector.select(candidates, query.applicationDate())
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}
