# Traceability report

Maps every requirement in `specs/01-requirements.md` to the concrete tests that verify it: the
frozen acceptance/architecture tests and the unit tests written while implementing `specs/05-tasks.md`.

## Requirements

| Requirement | Frozen tests | Unit tests |
|---|---|---|
| REQ-01 · Price query | `PriceApiAcceptanceTest.returnsApplicablePriceForStatementScenarios` (AC-01..AC-05) | `FindApplicablePriceServiceTest.passesTheQueryValuesUnchangedToThePort`, `.returnsTheSelectorWinner`; `PriceControllerTest.returns200WithTheApplicablePriceJsonShape`, `.buildsTheQueryFromTheRequestParameters` |
| REQ-02 · Applicability window | `PriceApiAcceptanceTest.returnsApplicablePriceForStatementScenarios`, `.appliesInclusiveValidityWindows` (AC-01..AC-06) | `PriceTest.isApplicableAtStartInclusive`, `.isApplicableAtEndInclusive`, `.isNotApplicableOneSecondBeforeStart`, `.isNotApplicableOneSecondAfterEnd`, `.isApplicableAtThrowsOnNullMoment`; `PriceSelectorTest.returnsEmptyWhenNoCandidateIsApplicableAtTheMoment`; `PricePersistenceAdapterTest.returnsExactlyTheOverlappingCandidatesAt1600`, `.includesTheInclusiveEndBoundaryOfList2`, `.excludesOneSecondAfterTheEndBoundaryOfList2` |
| REQ-03 · Priority | `PriceApiAcceptanceTest.returnsApplicablePriceForStatementScenarios` (AC-02, AC-04, AC-05) | `PriceSelectorTest.selectsTheHighestPriorityWhenWindowsOverlap`, `.statementScenarioTwo/Three/Four/Five`; `FindApplicablePriceServiceTest.returnsTheSelectorWinner` |
| REQ-04 · Tie-break (D-01) | none (seed data has no ties) | `PriceSelectorTest.resolvesAPriorityTieByTheHighestPriceList` |
| REQ-05 · No applicable price | `PriceApiAcceptanceTest.returnsNotFoundWhenNoPriceApplies` (AC-07) | `PriceNotFoundExceptionTest.exposesBrandProductAndApplicationDate`, `.isAnUncheckedException`; `FindApplicablePriceServiceTest.throwsPriceNotFoundExceptionCarryingTheQueryContextWhenNothingApplies`; `PriceControllerTest.returns404WithAProblemDetailsBodyWhenNoPriceApplies` |
| REQ-06 · Invalid input | `PriceApiAcceptanceTest.returnsBadRequestWhenParameterIsMissing`, `.returnsBadRequestWhenParameterIsMalformed`, `.returnsBadRequestWhenIdentifierIsNotPositive` (AC-08, AC-09, AC-10) | `FindApplicablePriceQueryTest` (all invariant tests); `PriceControllerTest.returns400WithAProblemDetailsBodyWhenAParameterIsMissing`, `.returns400WithAProblemDetailsBodyWhenTheDateIsMalformed`, `.returns400WithAProblemDetailsBodyWhenAnIdentifierIsNotPositive` |
| REQ-07 · Unsupported method | `PriceApiAcceptanceTest.returnsMethodNotAllowedForUnsupportedMethods` (AC-11) | `PriceControllerTest.returns405WithAllowHeaderForUnsupportedMethods` |
| REQ-08 · Unexpected error | none (cannot be triggered black-box) | `PriceControllerTest.returns500WithAGenericBodyThatDoesNotLeakTheExceptionMessage` |
| REQ-09 · Date-time format | `PriceApiAcceptanceTest.returnsApplicablePriceForStatementScenarios`, `.appliesInclusiveValidityWindows`, `.returnsBadRequestWhenParameterIsMalformed` (AC-01..AC-06, AC-09) | `PriceControllerTest.returns200WithTheApplicablePriceJsonShape` (ISO string fields), `.returns400WithAProblemDetailsBodyWhenTheDateIsMalformed` |
| REQ-10 · Seed data | `PriceApiAcceptanceTest.returnsApplicablePriceForStatementScenarios`, `.appliesInclusiveValidityWindows`, `.returnsNotFoundWhenNoPriceApplies` (AC-01..AC-07) | `PricePersistenceAdapterTest` (all methods, in particular `.mapsAmountAndCurrencyExactly`, `.returnsAnEmptyListForAnUnknownProduct`, `.returnsAnEmptyListForAnUnknownBrand`) |
| NFR-01 · Hexagonal architecture | `HexagonalArchitectureTest` (all 12 rules) | `ApplicationConfigTest.exposesAFindApplicablePriceUseCaseBackedByTheApplicationService`; `PricesApplicationTests.contextLoads` |
| NFR-02 · Money precision (`BigDecimal`) | `PriceApiAcceptanceTest` (amount assertions in every 200 scenario) | `PriceTest.createsAPriceWithValidValues`, `.rejectsNegativeAmount`, `.acceptsZeroAmount`; `PricePersistenceAdapterTest.mapsAmountAndCurrencyExactly`; `PriceControllerTest.returns200WithTheApplicablePriceJsonShape` |
| NFR-03 · Build quality gate | JaCoCo `check` goal bound to `verify` (bundle line coverage ≥ 80%) | — |
| NFR-04 · Read-only and stateless | Satisfied by design: the only endpoint is `GET /prices`, no write operations exist, and no request-scoped or shared mutable state is kept. Indirectly covered by every acceptance test (each runs against a freshly seeded, per-context H2 database) and by `HexagonalArchitectureTest` | — |

## Final `./mvnw verify` summary

- **Tests run:** 111
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- All frozen acceptance tests (`PriceApiAcceptanceTest`, AC-01..AC-11) and architecture tests
  (`HexagonalArchitectureTest`, 12 rules) are green.

## JaCoCo line coverage

- **Bundle line coverage: 100%** (105/105 lines covered across all production classes; `PricesApplication`
  is excluded from the report per `pom.xml`).
- Required by NFR-03: ≥ 80%. The `check` goal passes.

Per-class line coverage (all 100%): `Price`, `PriceSelector`, `PriceNotFoundException`,
`FindApplicablePriceQuery`, `FindApplicablePriceUseCase`, `LoadPricesPort`, `FindApplicablePriceService`,
`ApplicationConfig`, `PriceJpaEntity`, `SpringDataPriceRepository`, `PriceEntityMapper`,
`PricePersistenceAdapter`, `PriceController`, `PriceResponse`, `RestExceptionHandler`.

## Decisions applied

- **D-01 · Tie-break.** When two or more applicable prices share the highest priority, the one with
  the highest `priceList` wins (REQ-04). Implemented in `PriceSelector` via a comparator ordered by
  `priority` then `priceList`, taking the maximum. The seed data has no ties, so this is exercised only
  by `PriceSelectorTest.resolvesAPriorityTieByTheHighestPriceList`.
- **D-02 · Date-time format.** `applicationDate`, `startDate` and `endDate` use ISO-8601
  (`yyyy-MM-ddTHH:mm:ss`) in both directions; the space-separated format of the original 2023
  implementation is rejected with `400 Bad Request`. Implemented with
  `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` on `PriceController`'s `applicationDate`
  parameter and Spring Boot's default `LocalDateTime` Jackson serialisation for the response fields.
  Verified by `PriceApiAcceptanceTest.returnsBadRequestWhenParameterIsMalformed` (the
  `"2020-06-14 10:00:00"` case) and `PriceControllerTest.returns400WithAProblemDetailsBodyWhenTheDateIsMalformed`.

## Implementation notes

- `PriceController` does not carry `@Validated`: Spring Framework 6.1+ validates constraint
  annotations (`@Positive`) directly on `@RequestParam` method parameters and reports violations as
  `HandlerMethodValidationException`, which `ResponseEntityExceptionHandler` (the superclass of
  `RestExceptionHandler`) already maps to `400 Bad Request`. Adding `@Validated` at the class level
  instead activates the older AOP-based `MethodValidationPostProcessor`, which throws a plain
  `jakarta.validation.ConstraintViolationException` that is *not* one of the exceptions
  `ResponseEntityExceptionHandler` recognises, so it would fall through to the generic `500` handler.
  This was caught by `PriceControllerTest.returns400WithAProblemDetailsBodyWhenAnIdentifierIsNotPositive`
  during T08.
- Test-slice annotations (`@DataJpaTest`, `@WebMvcTest`) live under Spring Boot 4's new per-module
  packages (`org.springframework.boot.data.jpa.test.autoconfigure`,
  `org.springframework.boot.webmvc.test.autoconfigure`) rather than the historical
  `org.springframework.boot.test.autoconfigure.*` packages used in Spring Boot 3.

## Open questions

None. No spec was found ambiguous or contradictory during implementation; `specs/open-questions.md`
has no entries.
