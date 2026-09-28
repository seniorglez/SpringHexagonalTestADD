package com.seniorglez.prices.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    private static final long BRAND_ID = 1L;
    private static final long PRODUCT_ID = 35455L;
    private static final int PRICE_LIST = 1;
    private static final int PRIORITY = 0;
    private static final LocalDateTime START = LocalDateTime.parse("2020-06-14T00:00:00");
    private static final LocalDateTime END = LocalDateTime.parse("2020-12-31T23:59:59");
    private static final BigDecimal AMOUNT = new BigDecimal("35.50");
    private static final String CURRENCY = "EUR";

    private static Price validPrice() {
        return new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, CURRENCY);
    }

    @Test
    void createsAPriceWithValidValues() {
        Price price = validPrice();

        assertThat(price.brandId()).isEqualTo(BRAND_ID);
        assertThat(price.productId()).isEqualTo(PRODUCT_ID);
        assertThat(price.priceList()).isEqualTo(PRICE_LIST);
        assertThat(price.priority()).isEqualTo(PRIORITY);
        assertThat(price.startDate()).isEqualTo(START);
        assertThat(price.endDate()).isEqualTo(END);
        assertThat(price.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(price.currency()).isEqualTo(CURRENCY);
    }

    @Test
    void rejectsZeroBrandId() {
        assertThatThrownBy(() -> new Price(0, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBrandId() {
        assertThatThrownBy(() -> new Price(-1, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroProductId() {
        assertThatThrownBy(() -> new Price(BRAND_ID, 0, PRICE_LIST, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeProductId() {
        assertThatThrownBy(() -> new Price(BRAND_ID, -1, PRICE_LIST, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroPriceList() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, 0, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativePriceList() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, -1, PRIORITY, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativePriority() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, -1, START, END, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsZeroPriority() {
        Price price = new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, 0, START, END, AMOUNT, CURRENCY);
        assertThat(price.priority()).isZero();
    }

    @Test
    void rejectsNullStartDate() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, null, END, AMOUNT, CURRENCY))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullEndDate() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, null, AMOUNT, CURRENCY))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        LocalDateTime end = START.minusSeconds(1);
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, end, AMOUNT, CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsEqualStartAndEndDate() {
        Price price = new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, START, AMOUNT, CURRENCY);
        assertThat(price.startDate()).isEqualTo(price.endDate());
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, null, CURRENCY))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END,
                new BigDecimal("-0.01"), CURRENCY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsZeroAmount() {
        Price price = new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, BigDecimal.ZERO, CURRENCY);
        assertThat(price.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejectsNullCurrency() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsCurrencyThatIsNotThreeLetters() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, "EU"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLowerCaseCurrency() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, "eur"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCurrencyWithNonLetterCharacters() {
        assertThatThrownBy(() -> new Price(BRAND_ID, PRODUCT_ID, PRICE_LIST, PRIORITY, START, END, AMOUNT, "12E"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isApplicableAtStartInclusive() {
        assertThat(validPrice().isApplicableAt(START)).isTrue();
    }

    @Test
    void isApplicableAtEndInclusive() {
        assertThat(validPrice().isApplicableAt(END)).isTrue();
    }

    @Test
    void isNotApplicableOneSecondBeforeStart() {
        assertThat(validPrice().isApplicableAt(START.minusSeconds(1))).isFalse();
    }

    @Test
    void isNotApplicableOneSecondAfterEnd() {
        assertThat(validPrice().isApplicableAt(END.plusSeconds(1))).isFalse();
    }

    @Test
    void isApplicableAtThrowsOnNullMoment() {
        assertThatThrownBy(() -> validPrice().isApplicableAt(null))
                .isInstanceOf(NullPointerException.class);
    }
}
