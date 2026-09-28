# 01 · Requirements

Functional requirements are written in EARS style ("WHEN … THE SYSTEM SHALL …").
Every requirement is traced to at least one test (see the traceability matrix at the end).

## Context

A retail platform stores, for each brand and product, several **price lists** (tariffs). Each
tariff is valid within a date-time window and has a **priority**. Clients need the **final
price** that applies to a product of a brand at a given moment.

## Functional requirements

**REQ-01 · Price query.**
WHEN a client sends `GET /prices` with `applicationDate`, `productId` and `brandId`,
THE SYSTEM SHALL respond `200 OK` with the applicable price: `productId`, `brandId`, `priceList`,
`startDate`, `endDate`, `amount` and `currency`. The exact format is in `03-api-contract.md`.

**REQ-02 · Applicability window.**
A price SHALL be applicable to a query when its `brandId` and `productId` match the query and
`startDate ≤ applicationDate ≤ endDate`. Both ends are **inclusive** and the precision is one second.

**REQ-03 · Priority.**
WHEN more than one price is applicable,
THE SYSTEM SHALL select the one with the **highest** `priority` value.

**REQ-04 · Tie-break (decision D-01).**
WHEN two or more applicable prices share the highest priority,
THE SYSTEM SHALL select the one with the **highest** `priceList`.
*Rationale: the original statement does not define ties. A deterministic rule is preferred over an
error. Newer price lists have higher identifiers in the source data.*

**REQ-05 · No applicable price.**
WHEN no price is applicable,
THE SYSTEM SHALL respond `404 Not Found` with a problem-details body.

**REQ-06 · Invalid input.**
WHEN a required parameter is missing, cannot be parsed, or an identifier is not a positive integer,
THE SYSTEM SHALL respond `400 Bad Request` with a problem-details body, without executing the use case.

**REQ-07 · Unsupported method.**
WHEN a client uses any HTTP method other than `GET` on `/prices`,
THE SYSTEM SHALL respond `405 Method Not Allowed` with a problem-details body and an `Allow`
header that includes `GET`.

**REQ-08 · Unexpected error.**
WHEN an unexpected error occurs,
THE SYSTEM SHALL respond `500 Internal Server Error` with a **generic** problem-details body. The
body SHALL NOT contain exception messages, stack traces, class names or SQL. The error is logged
server-side with its stack trace.

**REQ-09 · Date-time format.**
THE SYSTEM SHALL accept `applicationDate` and SHALL return `startDate` and `endDate` as ISO-8601
local date-times (`yyyy-MM-ddTHH:mm:ss`). Other formats are rejected under REQ-06.
*Decision D-02: the first implementation documented ISO-8601 but parsed `yyyy-MM-dd HH:mm:ss`. The
spec now fixes ISO-8601 for both directions.*

**REQ-10 · Seed data.**
WHEN the application starts,
THE SYSTEM SHALL serve the four seed prices defined in `02-domain.md`, loaded into an in-memory H2
database from the frozen `schema.sql` and `data.sql`.

## Non-functional requirements

**NFR-01 · Hexagonal architecture.** As defined in the constitution, section 2. Verified by
`HexagonalArchitectureTest`.

**NFR-02 · Money precision.** Amounts are `BigDecimal` end to end and are serialised as JSON numbers
with their stored scale (for example `35.50`).

**NFR-03 · Build quality gate.** `./mvnw verify` passes, including a JaCoCo line coverage of at least
80% for the production code (the application class is excluded).

**NFR-04 · Read-only and stateless.** The API exposes no write operations and keeps no state
between requests.

## Acceptance criteria (frozen tests in `PriceApiAcceptanceTest`)

| AC | Scenario | Expected | Test method |
|---|---|---|---|
| AC-01 | 2020-06-14T10:00:00, product 35455, brand 1 | 200, price list 1, 35.50 EUR | `returnsApplicablePriceForStatementScenarios` |
| AC-02 | 2020-06-14T16:00:00 | 200, price list 2, 25.45 EUR | same |
| AC-03 | 2020-06-14T21:00:00 | 200, price list 1, 35.50 EUR | same |
| AC-04 | 2020-06-15T10:00:00 | 200, price list 3, 30.50 EUR | same |
| AC-05 | 2020-06-16T21:00:00 | 200, price list 4, 38.95 EUR | same |
| AC-06 | Window boundaries (start and end, ±1 s) | inclusive ends | `appliesInclusiveValidityWindows` |
| AC-07 | Outside all windows, unknown product, unknown brand | 404 problem | `returnsNotFoundWhenNoPriceApplies` |
| AC-08 | Each required parameter missing | 400 problem | `returnsBadRequestWhenParameterIsMissing` |
| AC-09 | Malformed date, non-numeric id, id out of 64-bit range | 400 problem | `returnsBadRequestWhenParameterIsMalformed` |
| AC-10 | Zero or negative id | 400 problem | `returnsBadRequestWhenIdentifierIsNotPositive` |
| AC-11 | POST, PUT, PATCH, DELETE | 405 problem + `Allow: GET` | `returnsMethodNotAllowedForUnsupportedMethods` |

Every 200 response is checked field by field: product, brand, price list, window, amount and currency.

## Traceability matrix

| Requirement | Frozen tests | Unit tests to be written (see tasks) |
|---|---|---|
| REQ-01 | AC-01…AC-05 | T04, T07 |
| REQ-02 | AC-01…AC-06 | T01, T05 |
| REQ-03 | AC-02, AC-04, AC-05 | T02 |
| REQ-04 | none (the seed data has no ties) | T02 |
| REQ-05 | AC-07 | T04, T08 |
| REQ-06 | AC-08, AC-09, AC-10 | T08 |
| REQ-07 | AC-11 | T08 |
| REQ-08 | none (cannot be triggered black-box) | T08 |
| REQ-09 | AC-01…AC-06, AC-09 | T07 |
| REQ-10 | AC-01…AC-07 | T05 |
| NFR-01 | `HexagonalArchitectureTest` | none |
| NFR-02 | AC-01…AC-06 | T01, T05, T07 |
| NFR-03 | JaCoCo check in `./mvnw verify` | T09 |
