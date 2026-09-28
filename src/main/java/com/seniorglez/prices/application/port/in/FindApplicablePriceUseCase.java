package com.seniorglez.prices.application.port.in;

import com.seniorglez.prices.domain.model.Price;

public interface FindApplicablePriceUseCase {

    Price find(FindApplicablePriceQuery query);
}
