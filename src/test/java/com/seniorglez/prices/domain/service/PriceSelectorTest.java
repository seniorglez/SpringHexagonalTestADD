package com.seniorglez.prices.domain.service;

import com.seniorglez.prices.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PriceSelectorTest {

    private static final long BRAND_ID = 1L;
    private static final long PRODUCT_ID = 35455L;
    private static final String CURRENCY = "EUR";

    private static final Price LIST_1 = price(1, 0, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50");
    private static final Price LIST_2 = price(2, 1, "2020-06-14T15:00:00", "2020-06-14T18:30:00", "25.45");
    private static final Price LIST_3 = price(3, 1, "2020-06-15T00:00:00", "2020-06-15T11:00:00", "30.50");
    private static final Price LIST_4 = price(4, 1, "2020-06-15T16:00:00", "2020-12-31T23:59:59", "38.95");

    private final PriceSelector selector = new PriceSelector();

    private static Price price(int priceList, int priority, String start, String end, String amount) {
        return new Price(BRAND_ID, PRODUCT_ID, priceList, priority,
                LocalDateTime.parse(start), LocalDateTime.parse(end), new BigDecimal(amount), CURRENCY);
    }

    @Test
    void returnsEmptyWhenThereAreNoCandidates() {
        Optional<Price> selected = selector.select(List.of(), LocalDateTime.parse("2020-06-14T10:00:00"));

        assertThat(selected).isEmpty();
    }

    @Test
    void returnsTheOnlyCandidateWhenItIsApplicable() {
        Optional<Price> selected = selector.select(List.of(LIST_1), LocalDateTime.parse("2020-06-14T10:00:00"));

        assertThat(selected).contains(LIST_1);
    }

    @Test
    void returnsEmptyWhenNoCandidateIsApplicableAtTheMoment() {
        Optional<Price> selected = selector.select(List.of(LIST_1, LIST_2), LocalDateTime.parse("2019-01-01T00:00:00"));

        assertThat(selected).isEmpty();
    }

    @Test
    void selectsTheHighestPriorityWhenWindowsOverlap() {
        Optional<Price> selected = selector.select(List.of(LIST_1, LIST_2), LocalDateTime.parse("2020-06-14T16:00:00"));

        assertThat(selected).contains(LIST_2);
    }

    @Test
    void resolvesAPriorityTieByTheHighestPriceList() {
        Price tiedLower = price(10, 1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "10.00");
        Price tiedHigher = price(20, 1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", "20.00");

        Optional<Price> selected = selector.select(List.of(tiedLower, tiedHigher), LocalDateTime.parse("2020-06-14T10:00:00"));

        assertThat(selected).contains(tiedHigher);
    }

    @Test
    void statementScenarioOne() {
        Optional<Price> selected = selector.select(List.of(LIST_1), LocalDateTime.parse("2020-06-14T10:00:00"));

        assertThat(selected).contains(LIST_1);
    }

    @Test
    void statementScenarioTwo() {
        Optional<Price> selected = selector.select(List.of(LIST_1, LIST_2), LocalDateTime.parse("2020-06-14T16:00:00"));

        assertThat(selected).contains(LIST_2);
    }

    @Test
    void statementScenarioThree() {
        Optional<Price> selected = selector.select(List.of(LIST_1), LocalDateTime.parse("2020-06-14T21:00:00"));

        assertThat(selected).contains(LIST_1);
    }

    @Test
    void statementScenarioFour() {
        Optional<Price> selected = selector.select(List.of(LIST_1, LIST_3), LocalDateTime.parse("2020-06-15T10:00:00"));

        assertThat(selected).contains(LIST_3);
    }

    @Test
    void statementScenarioFive() {
        Optional<Price> selected = selector.select(List.of(LIST_1, LIST_4), LocalDateTime.parse("2020-06-16T21:00:00"));

        assertThat(selected).contains(LIST_4);
    }
}
