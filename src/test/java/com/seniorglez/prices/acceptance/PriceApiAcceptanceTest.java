package com.seniorglez.prices.acceptance;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FROZEN acceptance tests: human-authored and part of the baseline. Do not modify this file.
 *
 * <p>These tests are the oracle for {@code specs/01-requirements.md} (AC-01 … AC-11). They are black-box on
 * purpose. They only use real HTTP and the public contract in {@code specs/03-api-contract.md}, so they
 * compile before any production code exists and do not constrain the internal design. They rely on no
 * framework test slice, which keeps them stable across Spring Boot versions.
 *
 * <p>Seed data: {@code specs/02-domain.md}, loaded from the frozen {@code schema.sql} and {@code data.sql}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PriceApiAcceptanceTest {

    private static final String PRODUCT_ID = "35455";
    private static final String BRAND_ID = "1";

    /** Expected tariffs of the seed data, keyed by price list. */
    private static final Map<Integer, Tariff> SEED_TARIFFS = Map.of(
            1, new Tariff("2020-06-14T00:00:00", "2020-12-31T23:59:59", "35.50"),
            2, new Tariff("2020-06-14T15:00:00", "2020-06-14T18:30:00", "25.45"),
            3, new Tariff("2020-06-15T00:00:00", "2020-06-15T11:00:00", "30.50"),
            4, new Tariff("2020-06-15T16:00:00", "2020-12-31T23:59:59", "38.95"));

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private Environment environment;

    // ------------------------------------------------------------------------------------------- 200 OK

    @DisplayName("AC-01..AC-05 · the five scenarios of the original statement")
    @ParameterizedTest(name = "{0} -> price list {1}")
    @CsvSource({
            "2020-06-14T10:00:00, 1",
            "2020-06-14T16:00:00, 2",
            "2020-06-14T21:00:00, 1",
            "2020-06-15T10:00:00, 3",
            "2020-06-16T21:00:00, 4"
    })
    void returnsApplicablePriceForStatementScenarios(String applicationDate, int expectedPriceList) throws Exception {
        assertApplicablePrice(get(applicationDate, PRODUCT_ID, BRAND_ID), expectedPriceList);
    }

    @DisplayName("AC-06 · validity windows are inclusive at both ends")
    @ParameterizedTest(name = "{0} -> price list {1}")
    @CsvSource({
            "2020-06-14T00:00:00, 1",
            "2020-06-14T14:59:59, 1",
            "2020-06-14T15:00:00, 2",
            "2020-06-14T18:30:00, 2",
            "2020-06-14T18:30:01, 1",
            "2020-06-15T00:00:00, 3",
            "2020-06-15T11:00:00, 3",
            "2020-06-15T11:00:01, 1",
            "2020-06-15T15:59:59, 1",
            "2020-06-15T16:00:00, 4",
            "2020-12-31T23:59:59, 4"
    })
    void appliesInclusiveValidityWindows(String applicationDate, int expectedPriceList) throws Exception {
        assertApplicablePrice(get(applicationDate, PRODUCT_ID, BRAND_ID), expectedPriceList);
    }

    // ---------------------------------------------------------------------------------------------- 404

    @DisplayName("AC-07 · 404 when no price applies")
    @ParameterizedTest(name = "date={0}, product={1}, brand={2}")
    @CsvSource({
            "2020-06-13T23:59:59, 35455, 1",   // one second before every window
            "2021-01-01T00:00:00, 35455, 1",   // one second after every window
            "2020-06-14T10:00:00, 99999, 1",   // unknown product
            "2020-06-14T10:00:00, 35455, 2"    // unknown brand
    })
    void returnsNotFoundWhenNoPriceApplies(String applicationDate, String productId, String brandId) throws Exception {
        assertProblem(get(applicationDate, productId, brandId), 404);
    }

    // ---------------------------------------------------------------------------------------------- 400

    @DisplayName("AC-08 · 400 when a required parameter is missing")
    @ParameterizedTest(name = "date={0}, product={1}, brand={2}")
    @CsvSource(nullValues = "MISSING", value = {
            "MISSING,             35455,   1",
            "2020-06-14T10:00:00, MISSING, 1",
            "2020-06-14T10:00:00, 35455,   MISSING"
    })
    void returnsBadRequestWhenParameterIsMissing(String applicationDate, String productId, String brandId) throws Exception {
        assertProblem(get(applicationDate, productId, brandId), 400);
    }

    @DisplayName("AC-09 · 400 when a parameter is malformed")
    @ParameterizedTest(name = "date={0}, product={1}, brand={2}")
    @CsvSource({
            "notADate,            35455,                1",
            "2020-06-14 10:00:00, 35455,                1",   // format accepted by the first implementation
            "2020-06-14,          35455,                1",   // date without time
            "2020-06-14T10:00:00, abc,                  1",
            "2020-06-14T10:00:00, 35455,                abc",
            "2020-06-14T10:00:00, 99999999999999999999, 1"    // outside the 64-bit range
    })
    void returnsBadRequestWhenParameterIsMalformed(String applicationDate, String productId, String brandId) throws Exception {
        assertProblem(get(applicationDate, productId, brandId), 400);
    }

    @DisplayName("AC-10 · 400 when an identifier is not positive")
    @ParameterizedTest(name = "product={0}, brand={1}")
    @CsvSource({
            "0,     1",
            "-1,    1",
            "35455, 0",
            "35455, -5"
    })
    void returnsBadRequestWhenIdentifierIsNotPositive(String productId, String brandId) throws Exception {
        assertProblem(get("2020-06-14T10:00:00", productId, brandId), 400);
    }

    // ---------------------------------------------------------------------------------------------- 405

    @DisplayName("AC-11 · 405 with Allow header for unsupported methods")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void returnsMethodNotAllowedForUnsupportedMethods(String method) throws Exception {
        HttpResponse<String> response = send(method, parameters("2020-06-14T10:00:00", PRODUCT_ID, BRAND_ID));

        assertProblem(response, 405);
        assertThat(response.headers().firstValue("Allow"))
                .as("Allow header")
                .hasValueSatisfying(allow -> assertThat(allow).contains("GET"));
    }

    // ------------------------------------------------------------------------------------------ helpers

    private void assertApplicablePrice(HttpResponse<String> response, int expectedPriceList) {
        assertThat(response.statusCode()).as("HTTP status, body: %s", response.body()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("application/json"));

        DocumentContext json = JsonPath.parse(response.body());
        Tariff expected = SEED_TARIFFS.get(expectedPriceList);

        assertThat(number(json, "$.productId")).isEqualByComparingTo(PRODUCT_ID);
        assertThat(number(json, "$.brandId")).isEqualByComparingTo(BRAND_ID);
        assertThat(number(json, "$.priceList")).isEqualByComparingTo(String.valueOf(expectedPriceList));
        assertThat(dateTime(json, "$.startDate")).isEqualTo(LocalDateTime.parse(expected.startDate()));
        assertThat(dateTime(json, "$.endDate")).isEqualTo(LocalDateTime.parse(expected.endDate()));
        assertThat(number(json, "$.amount")).isEqualByComparingTo(expected.amount());
        assertThat(json.read("$.currency", String.class)).isEqualTo("EUR");
    }

    private void assertProblem(HttpResponse<String> response, int expectedStatus) {
        assertThat(response.statusCode()).as("HTTP status, body: %s", response.body()).isEqualTo(expectedStatus);
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(contentType -> assertThat(contentType).startsWith("application/problem+json"));

        DocumentContext json = JsonPath.parse(response.body());
        assertThat(number(json, "$.status")).isEqualByComparingTo(String.valueOf(expectedStatus));
        assertThat(json.read("$.title", String.class)).isNotBlank();
    }

    private HttpResponse<String> get(String applicationDate, String productId, String brandId) throws Exception {
        return send("GET", parameters(applicationDate, productId, brandId));
    }

    private HttpResponse<String> send(String method, Map<String, String> parameters) throws Exception {
        String query = parameters.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        String port = environment.getRequiredProperty("local.server.port");
        URI uri = URI.create("http://localhost:" + port + "/prices" + (query.isEmpty() ? "" : "?" + query));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Builds the query parameters, leaving out the null ones (to simulate missing parameters). */
    private static Map<String, String> parameters(String applicationDate, String productId, String brandId) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (applicationDate != null) {
            parameters.put("applicationDate", applicationDate);
        }
        if (productId != null) {
            parameters.put("productId", productId);
        }
        if (brandId != null) {
            parameters.put("brandId", brandId);
        }
        return parameters;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static BigDecimal number(DocumentContext json, String path) {
        Object value = json.read(path);
        assertThat(value).as("JSON field %s must be a number", path).isInstanceOf(Number.class);
        return new BigDecimal(value.toString());
    }

    private static LocalDateTime dateTime(DocumentContext json, String path) {
        return LocalDateTime.parse(json.read(path, String.class));
    }

    private record Tariff(String startDate, String endDate, String amount) {
    }
}
