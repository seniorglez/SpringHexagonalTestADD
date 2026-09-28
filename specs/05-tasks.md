# 05 · Tasks

Work queue for the implementer. Do the tasks **in order, one at a time**, following the workflow in
`CLAUDE.md`. Only the checkboxes of this file may change. The gate compares the task lines with the
baseline.

Legend: *Done when* lists the tests that must exist and be green, in addition to "no regressions".

---

- [x] **T01** — Domain model `Price` with invariants and `isApplicableAt`
  - Requirements: REQ-02, NFR-02
  - Files: `domain/model/Price`
  - Done when: `PriceTest` covers every invariant in `02-domain.md` (one test per rejected value) and
    `isApplicableAt` at start (inclusive), end (inclusive), one second before start and one second
    after end.

- [x] **T02** — Selection policy `PriceSelector`
  - Requirements: REQ-03, REQ-04
  - Files: `domain/service/PriceSelector`
  - Done when: `PriceSelectorTest` covers no candidates, a single candidate, candidates not applicable
    at the moment, highest priority wins, the priority tie resolved by the highest price list, and
    the five worked examples of `02-domain.md` built as in-memory `Price` objects inside the test.

- [x] **T03** — Domain exception, application ports and query
  - Requirements: REQ-05, NFR-01
  - Files: `domain/exception/PriceNotFoundException`, `application/port/in/FindApplicablePriceUseCase`,
    `application/port/in/FindApplicablePriceQuery`, `application/port/out/LoadPricesPort`
  - Done when: `FindApplicablePriceQueryTest` covers its invariants, and `PriceNotFoundException`
    exposes brand, product and date (tested).

- [x] **T04** — Application service `FindApplicablePriceService`
  - Requirements: REQ-01, REQ-03, REQ-05
  - Files: `application/service/FindApplicablePriceService`
  - Done when: `FindApplicablePriceServiceTest`, using a hand-written in-memory `LoadPricesPort`,
    verifies that the query values reach the port unchanged, that the selector's winner is returned,
    and that `PriceNotFoundException` carrying the query context is thrown when nothing applies.

- [x] **T05** — Persistence adapter
  - Requirements: REQ-02, REQ-10, NFR-02
  - Files: everything in `infrastructure/adapter/out/persistence`, plus `application.properties`
    (settings from `04-design.md`)
  - Done when: the application starts with `ddl-auto=validate`, and `PricePersistenceAdapterTest`
    against the seed shows that the candidates at 2020-06-14T16:00:00 are exactly price lists {1, 2},
    that the boundaries 2020-06-14T18:30:00 and 2020-06-14T18:30:01 behave inclusively, that an
    unknown product or brand gives an empty list, and that mapping keeps `25.45`/`EUR` exactly.

- [x] **T06** — Wiring `ApplicationConfig`
  - Requirements: NFR-01
  - Files: `infrastructure/config/ApplicationConfig`
  - Done when: a test proves that the Spring context exposes a `FindApplicablePriceUseCase` bean
    backed by `FindApplicablePriceService`, and the generated `PricesApplicationTests` stays green.

- [x] **T07** — REST adapter, happy path
  - Requirements: REQ-01, REQ-09, NFR-02
  - Files: `PriceController`, `PriceResponse`
  - Done when: `PriceControllerTest` checks the JSON shape and values of a 200 response, and the
    frozen acceptance tests `returnsApplicablePriceForStatementScenarios` and
    `appliesInclusiveValidityWindows` are green.

- [ ] **T08** — Error handling
  - Requirements: REQ-05, REQ-06, REQ-07, REQ-08
  - Files: `RestExceptionHandler`
  - Done when: `PriceControllerTest` also covers 404, the 400 families, 405 with `Allow`, and a 500
    whose body does not contain the message of the thrown exception (for example, a fake use case
    throwing `new RuntimeException("secret-internal-detail")`). **All** frozen acceptance tests are green.

- [ ] **T09** — Full verification
  - Requirements: NFR-01, NFR-03
  - Files: none expected. Fix whatever the checks reveal.
  - Done when: `HexagonalArchitectureTest` is green, the JaCoCo check passes, and `./mvnw verify` is
    green with no test skipped. Remove any dead code and any unused class.

- [ ] **T10** — Traceability report
  - Requirements: all
  - Files: `docs/traceability.md`
  - Done when: the report contains a table mapping every REQ/NFR to the concrete test classes and
    methods that verify it (frozen and yours), the final `./mvnw verify` test summary (tests run,
    failures, skipped), the line coverage reported by JaCoCo, and the decisions applied (D-01, D-02)
    plus any open question.
