package com.seniorglez.prices.infrastructure.adapter.in.rest;

import com.seniorglez.prices.domain.model.Price;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        long productId,
        long brandId,
        int priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal amount,
        String currency) {

    public static PriceResponse from(Price price) {
        return new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.amount(),
                price.currency());
    }
}
