package com.seniorglez.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public record Price(
        long brandId,
        long productId,
        int priceList,
        int priority,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal amount,
        String currency) {

    private static final Pattern CURRENCY_CODE = Pattern.compile("[A-Z]{3}");

    public Price {
        if (brandId <= 0) {
            throw new IllegalArgumentException("brandId must be > 0");
        }
        if (productId <= 0) {
            throw new IllegalArgumentException("productId must be > 0");
        }
        if (priceList <= 0) {
            throw new IllegalArgumentException("priceList must be > 0");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("priority must be >= 0");
        }
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        Objects.requireNonNull(currency, "currency must not be null");
        if (!CURRENCY_CODE.matcher(currency).matches()) {
            throw new IllegalArgumentException("currency must be a three-letter upper-case ISO-4217 code");
        }
    }

    public boolean isApplicableAt(LocalDateTime moment) {
        Objects.requireNonNull(moment, "moment must not be null");
        return !moment.isBefore(startDate) && !moment.isAfter(endDate);
    }
}
