package com.seniorglez.prices.infrastructure.adapter.out.persistence;

import com.seniorglez.prices.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PricePersistenceAdapterTest {

    private static final long BRAND_ID = 1L;
    private static final long PRODUCT_ID = 35455L;

    @Autowired
    private SpringDataPriceRepository repository;

    private PricePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PricePersistenceAdapter(repository, new PriceEntityMapper());
    }

    @Test
    void returnsExactlyTheOverlappingCandidatesAt1600() {
        List<Price> candidates = adapter.loadCandidates(BRAND_ID, PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00"));

        assertThat(candidates).extracting(Price::priceList).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void includesTheInclusiveEndBoundaryOfList2() {
        List<Price> candidates = adapter.loadCandidates(BRAND_ID, PRODUCT_ID, LocalDateTime.parse("2020-06-14T18:30:00"));

        assertThat(candidates).extracting(Price::priceList).contains(2);
    }

    @Test
    void excludesOneSecondAfterTheEndBoundaryOfList2() {
        List<Price> candidates = adapter.loadCandidates(BRAND_ID, PRODUCT_ID, LocalDateTime.parse("2020-06-14T18:30:01"));

        assertThat(candidates).extracting(Price::priceList).doesNotContain(2);
    }

    @Test
    void returnsAnEmptyListForAnUnknownProduct() {
        List<Price> candidates = adapter.loadCandidates(BRAND_ID, 99999L, LocalDateTime.parse("2020-06-14T16:00:00"));

        assertThat(candidates).isEmpty();
    }

    @Test
    void returnsAnEmptyListForAnUnknownBrand() {
        List<Price> candidates = adapter.loadCandidates(2L, PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00"));

        assertThat(candidates).isEmpty();
    }

    @Test
    void mapsAmountAndCurrencyExactly() {
        List<Price> candidates = adapter.loadCandidates(BRAND_ID, PRODUCT_ID, LocalDateTime.parse("2020-06-14T16:00:00"));

        Price list2 = candidates.stream().filter(price -> price.priceList() == 2).findFirst().orElseThrow();

        assertThat(list2.amount()).isEqualByComparingTo("25.45");
        assertThat(list2.currency()).isEqualTo("EUR");
    }
}
