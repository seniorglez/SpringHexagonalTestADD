package com.seniorglez.prices.infrastructure.config;

import com.seniorglez.prices.application.port.in.FindApplicablePriceUseCase;
import com.seniorglez.prices.application.service.FindApplicablePriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationConfigTest {

    @Autowired
    private FindApplicablePriceUseCase useCase;

    @Test
    void exposesAFindApplicablePriceUseCaseBackedByTheApplicationService() {
        assertThat(useCase).isInstanceOf(FindApplicablePriceService.class);
    }
}
