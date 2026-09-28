package com.seniorglez.prices.domain.service;

import com.seniorglez.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public class PriceSelector {

    private static final Comparator<Price> WINNER_ORDER =
            Comparator.comparingInt(Price::priority).thenComparingInt(Price::priceList);

    public Optional<Price> select(Collection<Price> candidates, LocalDateTime moment) {
        return candidates.stream()
                .filter(candidate -> candidate.isApplicableAt(moment))
                .max(WINNER_ORDER);
    }
}
