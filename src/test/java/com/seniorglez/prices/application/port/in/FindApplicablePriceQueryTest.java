package com.seniorglez.prices.application.port.in;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindApplicablePriceQueryTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.parse("2020-06-14T10:00:00");

    @Test
    void createsAQueryWithValidValues() {
        FindApplicablePriceQuery query = new FindApplicablePriceQuery(1L, 35455L, APPLICATION_DATE);

        assertThat(query.brandId()).isEqualTo(1L);
        assertThat(query.productId()).isEqualTo(35455L);
        assertThat(query.applicationDate()).isEqualTo(APPLICATION_DATE);
    }

    @Test
    void rejectsZeroBrandId() {
        assertThatThrownBy(() -> new FindApplicablePriceQuery(0, 35455L, APPLICATION_DATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBrandId() {
        assertThatThrownBy(() -> new FindApplicablePriceQuery(-1, 35455L, APPLICATION_DATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroProductId() {
        assertThatThrownBy(() -> new FindApplicablePriceQuery(1L, 0, APPLICATION_DATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeProductId() {
        assertThatThrownBy(() -> new FindApplicablePriceQuery(1L, -1, APPLICATION_DATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullApplicationDate() {
        assertThatThrownBy(() -> new FindApplicablePriceQuery(1L, 35455L, null))
                .isInstanceOf(NullPointerException.class);
    }
}
