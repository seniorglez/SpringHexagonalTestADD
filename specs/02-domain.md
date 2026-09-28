# 02 · Domain

## Glossary

| Term | Meaning |
|---|---|
| **Brand** | Retail brand that owns the catalogue (identified by `brandId`, for example `1`). |
| **Product** | Catalogue item (identified by `productId`, for example `35455`). |
| **Price list** (tariff) | Identifier of a pricing rule (`priceList`). One price list defines one price row. |
| **Validity window** | `[startDate, endDate]`, inclusive at both ends, one-second precision. |
| **Priority** | Disambiguator when windows overlap. A higher value wins. |
| **Applicable price** | The single price a customer pays for a product of a brand at a given moment. |

## Model: `Price`

| Field | Type | Invariant |
|---|---|---|
| `brandId` | `long` | > 0 |
| `productId` | `long` | > 0 |
| `priceList` | `int` | > 0 |
| `priority` | `int` | ≥ 0 |
| `startDate` | `LocalDateTime` | not null |
| `endDate` | `LocalDateTime` | not null, `startDate ≤ endDate` |
| `amount` | `BigDecimal` | not null, ≥ 0 |
| `currency` | `String` | not null, three upper-case letters (ISO-4217 code, e.g. `EUR`) |

Behaviour: `boolean isApplicableAt(LocalDateTime moment)` returns true if and only if
`startDate ≤ moment ≤ endDate` (REQ-02). A null `moment` is a programming error and throws.

A price does not know "which query" it answers. Brand and product matching is done by whoever
provides the candidates.

## Selection policy: `PriceSelector`

`Optional<Price> select(Collection<Price> candidates, LocalDateTime moment)`

1. Keep only the candidates where `isApplicableAt(moment)` is true. The persistence adapter
   pre-filters too, but the domain does not rely on that.
2. If none remain, return `Optional.empty()`.
3. Otherwise return the one with the highest `priority` (REQ-03).
4. On a priority tie, return the one with the highest `priceList` (REQ-04, decision D-01).

The selector is a stateless, pure function. It performs no I/O.

## Domain exception: `PriceNotFoundException`

An unchecked exception raised when no price applies. It carries `brandId`, `productId` and
`applicationDate` so that adapters can build a meaningful message (REQ-05).

## Seed data (frozen in `schema.sql` and `data.sql`)

| priceList | brandId | productId | priority | startDate | endDate | amount | currency |
|---|---|---|---|---|---|---|---|
| 1 | 1 | 35455 | 0 | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50 | EUR |
| 2 | 1 | 35455 | 1 | 2020-06-14T15:00:00 | 2020-06-14T18:30:00 | 25.45 | EUR |
| 3 | 1 | 35455 | 1 | 2020-06-15T00:00:00 | 2020-06-15T11:00:00 | 30.50 | EUR |
| 4 | 1 | 35455 | 1 | 2020-06-15T16:00:00 | 2020-12-31T23:59:59 | 38.95 | EUR |

## Worked examples (why each acceptance scenario returns what it returns)

| Moment | Applicable lists | Winner | Reason |
|---|---|---|---|
| 2020-06-14T10:00:00 | 1 | 1 | only candidate |
| 2020-06-14T16:00:00 | 1, 2 | 2 | priority 1 > 0 |
| 2020-06-14T21:00:00 | 1 | 1 | list 2 ended at 18:30:00 |
| 2020-06-15T10:00:00 | 1, 3 | 3 | priority 1 > 0 |
| 2020-06-16T21:00:00 | 1, 4 | 4 | priority 1 > 0 |
| 2020-06-14T18:30:00 | 1, 2 | 2 | the end is inclusive |
| 2020-06-15T11:00:01 | 1 | 1 | list 3 ended one second earlier |
