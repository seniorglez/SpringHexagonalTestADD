package com.seniorglez.prices.infrastructure.adapter.in.rest;

import com.seniorglez.prices.application.port.in.FindApplicablePriceQuery;
import com.seniorglez.prices.application.port.in.FindApplicablePriceUseCase;
import com.seniorglez.prices.domain.exception.PriceNotFoundException;
import com.seniorglez.prices.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindApplicablePriceUseCase useCase;

    @Test
    void returns200WithTheApplicablePriceJsonShape() throws Exception {
        Price price = new Price(1L, 35455L, 1, 0,
                LocalDateTime.parse("2020-06-14T00:00:00"), LocalDateTime.parse("2020-12-31T23:59:59"),
                new BigDecimal("35.50"), "EUR");
        when(useCase.find(any())).thenReturn(price);

        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T00:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59"))
                .andExpect(jsonPath("$.amount").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void buildsTheQueryFromTheRequestParameters() throws Exception {
        Price price = new Price(2L, 100L, 3, 1,
                LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2020-12-31T23:59:59"),
                new BigDecimal("10.00"), "EUR");
        when(useCase.find(any())).thenReturn(price);

        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "100")
                        .param("brandId", "2"))
                .andExpect(status().isOk());

        ArgumentCaptor<FindApplicablePriceQuery> captor = ArgumentCaptor.forClass(FindApplicablePriceQuery.class);
        verify(useCase).find(captor.capture());
        assertThat(captor.getValue().brandId()).isEqualTo(2L);
        assertThat(captor.getValue().productId()).isEqualTo(100L);
        assertThat(captor.getValue().applicationDate()).isEqualTo(LocalDateTime.parse("2020-06-14T10:00:00"));
    }

    @Test
    void returns404WithAProblemDetailsBodyWhenNoPriceApplies() throws Exception {
        when(useCase.find(any())).thenThrow(
                new PriceNotFoundException(1L, 35455L, LocalDateTime.parse("2019-01-01T00:00:00")));

        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2019-01-01T00:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    void returns400WithAProblemDetailsBodyWhenAParameterIsMissing() throws Exception {
        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns400WithAProblemDetailsBodyWhenTheDateIsMalformed() throws Exception {
        mockMvc.perform(get("/prices")
                        .param("applicationDate", "notADate")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns400WithAProblemDetailsBodyWhenAnIdentifierIsNotPositive() throws Exception {
        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "0")
                        .param("brandId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns405WithAllowHeaderForUnsupportedMethods() throws Exception {
        mockMvc.perform(post("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));

        mockMvc.perform(delete("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void returns500WithAGenericBodyThatDoesNotLeakTheExceptionMessage() throws Exception {
        when(useCase.find(any())).thenThrow(new RuntimeException("secret-internal-detail"));

        mockMvc.perform(get("/prices")
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret-internal-detail"))));
    }
}
