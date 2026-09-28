package com.seniorglez.prices.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    private final long brandId;
    private final long productId;
    private final LocalDateTime applicationDate;

    public PriceNotFoundException(long brandId, long productId, LocalDateTime applicationDate) {
        super("No applicable price for product %d of brand %d at %s"
                .formatted(productId, brandId, applicationDate));
        this.brandId = brandId;
        this.productId = productId;
        this.applicationDate = applicationDate;
    }

    public long brandId() {
        return brandId;
    }

    public long productId() {
        return productId;
    }

    public LocalDateTime applicationDate() {
        return applicationDate;
    }
}
