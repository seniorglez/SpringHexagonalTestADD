package com.seniorglez.prices.domain.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PriceNotFoundExceptionTest {

    @Test
    void exposesBrandProductAndApplicationDate() {
        LocalDateTime applicationDate = LocalDateTime.parse("2019-01-01T00:00:00");

        PriceNotFoundException exception = new PriceNotFoundException(1L, 35455L, applicationDate);

        assertThat(exception.brandId()).isEqualTo(1L);
        assertThat(exception.productId()).isEqualTo(35455L);
        assertThat(exception.applicationDate()).isEqualTo(applicationDate);
    }

    @Test
    void isAnUncheckedException() {
        PriceNotFoundException exception = new PriceNotFoundException(1L, 35455L, LocalDateTime.now());

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
