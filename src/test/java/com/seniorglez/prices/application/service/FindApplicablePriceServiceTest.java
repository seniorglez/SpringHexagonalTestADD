package com.seniorglez.prices.application.service;

import com.seniorglez.prices.application.port.in.FindApplicablePriceQuery;
import com.seniorglez.prices.application.port.out.LoadPricesPort;
import com.seniorglez.prices.domain.exception.PriceNotFoundException;
import com.seniorglez.prices.domain.model.Price;
import com.seniorglez.prices.domain.service.PriceSelector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindApplicablePriceServiceTest {

    private static final long BRAND_ID = 1L;
    private static final long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.parse("2020-06-14T16:00:00");

    private final PriceSelector selector = new PriceSelector();

    @Test
    void passesTheQueryValuesUnchangedToThePort() {
        RecordingLoadPricesPort port = new RecordingLoadPricesPort(List.of());
        FindApplicablePriceService service = new FindApplicablePriceService(port, selector);
        FindApplicablePriceQuery query = new FindApplicablePriceQuery(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);

        assertThatThrownBy(() -> service.find(query)).isInstanceOf(PriceNotFoundException.class);

        assertThat(port.receivedBrandId).isEqualTo(BRAND_ID);
        assertThat(port.receivedProductId).isEqualTo(PRODUCT_ID);
        assertThat(port.receivedAt).isEqualTo(APPLICATION_DATE);
    }

    @Test
    void returnsTheSelectorWinner() {
        Price lowerPriority = price(1, 0);
        Price higherPriority = price(2, 1);
        RecordingLoadPricesPort port = new RecordingLoadPricesPort(List.of(lowerPriority, higherPriority));
        FindApplicablePriceService service = new FindApplicablePriceService(port, selector);

        Price result = service.find(new FindApplicablePriceQuery(BRAND_ID, PRODUCT_ID, APPLICATION_DATE));

        assertThat(result).isEqualTo(higherPriority);
    }

    @Test
    void throwsPriceNotFoundExceptionCarryingTheQueryContextWhenNothingApplies() {
        RecordingLoadPricesPort port = new RecordingLoadPricesPort(List.of());
        FindApplicablePriceService service = new FindApplicablePriceService(port, selector);

        assertThatThrownBy(() -> service.find(new FindApplicablePriceQuery(BRAND_ID, PRODUCT_ID, APPLICATION_DATE)))
                .isInstanceOf(PriceNotFoundException.class)
                .satisfies(exception -> {
                    PriceNotFoundException notFound = (PriceNotFoundException) exception;
                    assertThat(notFound.brandId()).isEqualTo(BRAND_ID);
                    assertThat(notFound.productId()).isEqualTo(PRODUCT_ID);
                    assertThat(notFound.applicationDate()).isEqualTo(APPLICATION_DATE);
                });
    }

    private static Price price(int priceList, int priority) {
        return new Price(BRAND_ID, PRODUCT_ID, priceList, priority, APPLICATION_DATE.minusDays(1),
                APPLICATION_DATE.plusDays(1), new BigDecimal("10.00"), "EUR");
    }

    private static final class RecordingLoadPricesPort implements LoadPricesPort {
        private final List<Price> candidates;
        private long receivedBrandId;
        private long receivedProductId;
        private LocalDateTime receivedAt;

        private RecordingLoadPricesPort(List<Price> candidates) {
            this.candidates = candidates;
        }

        @Override
        public List<Price> loadCandidates(long brandId, long productId, LocalDateTime at) {
            this.receivedBrandId = brandId;
            this.receivedProductId = productId;
            this.receivedAt = at;
            return candidates;
        }
    }
}
