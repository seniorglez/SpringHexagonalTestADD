package com.seniorglez.prices.infrastructure.adapter.in.rest;

import com.seniorglez.prices.application.port.in.FindApplicablePriceQuery;
import com.seniorglez.prices.application.port.in.FindApplicablePriceUseCase;
import com.seniorglez.prices.domain.model.Price;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Validated
public class PriceController {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;

    public PriceController(FindApplicablePriceUseCase findApplicablePriceUseCase) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
    }

    @GetMapping("/prices")
    public PriceResponse findApplicablePrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam @Positive long productId,
            @RequestParam @Positive long brandId) {

        Price price = findApplicablePriceUseCase.find(
                new FindApplicablePriceQuery(brandId, productId, applicationDate));

        return PriceResponse.from(price);
    }
}
