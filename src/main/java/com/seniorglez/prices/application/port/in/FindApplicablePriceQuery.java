package com.seniorglez.prices.application.port.in;

import java.time.LocalDateTime;
import java.util.Objects;

public record FindApplicablePriceQuery(long brandId, long productId, LocalDateTime applicationDate) {

    public FindApplicablePriceQuery {
        if (brandId <= 0) {
            throw new IllegalArgumentException("brandId must be > 0");
        }
        if (productId <= 0) {
            throw new IllegalArgumentException("productId must be > 0");
        }
        Objects.requireNonNull(applicationDate, "applicationDate must not be null");
    }
}
