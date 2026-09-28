package com.seniorglez.prices.application.port.out;

import com.seniorglez.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface LoadPricesPort {

    List<Price> loadCandidates(long brandId, long productId, LocalDateTime at);
}
