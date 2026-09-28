package com.seniorglez.prices.infrastructure.config;

import com.seniorglez.prices.application.port.in.FindApplicablePriceUseCase;
import com.seniorglez.prices.application.port.out.LoadPricesPort;
import com.seniorglez.prices.application.service.FindApplicablePriceService;
import com.seniorglez.prices.domain.service.PriceSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public PriceSelector priceSelector() {
        return new PriceSelector();
    }

    @Bean
    public FindApplicablePriceUseCase findApplicablePriceUseCase(
            LoadPricesPort loadPricesPort, PriceSelector priceSelector) {
        return new FindApplicablePriceService(loadPricesPort, priceSelector);
    }
}
