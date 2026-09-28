# 03 · API contract

Base URL: `http://localhost:8080`. No authentication. No versioning prefix.

## `GET /prices`

Returns the applicable price for a product of a brand at a given moment.

### Query parameters (all required)

| Name | Type | Rules | Example |
|---|---|---|---|
| `applicationDate` | ISO-8601 local date-time | `yyyy-MM-ddTHH:mm:ss`, no offset or zone | `2020-06-14T10:00:00` |
| `productId` | integer (64-bit) | > 0 | `35455` |
| `brandId` | integer (64-bit) | > 0 | `1` |

Unknown extra parameters are ignored.

### `200 OK` · `Content-Type: application/json`

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "amount": 35.50,
  "currency": "EUR"
}
```

| Field | JSON type | Notes |
|---|---|---|
| `productId`, `brandId` | number (integer) | echo of the matched price |
| `priceList` | number (integer) | identifies the tariff that won |
| `startDate`, `endDate` | string | ISO-8601 local date-time |
| `amount` | number | decimal, stored scale kept (NFR-02) |
| `currency` | string | ISO-4217 code |

No other fields. In particular, `priority` and internal ids are **not** exposed.

### Errors · `Content-Type: application/problem+json` (RFC 9457)

Every error body is a problem-details document:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No applicable price for product 35455 of brand 1 at 2019-01-01T00:00:00",
  "instance": "/prices"
}
```

`status` always equals the HTTP status code. `title` is never blank.

| Status | When | `detail` |
|---|---|---|
| 400 | missing parameter, unparseable value, id ≤ 0, id outside the 64-bit range (REQ-06, REQ-09) | names the offending parameter; never echoes stack traces |
| 404 | no applicable price (REQ-05) | brand, product and date-time of the query |
| 405 | method other than `GET` (REQ-07) | generic; the response carries an `Allow` header that includes `GET` |
| 500 | anything unexpected (REQ-08) | exactly `An unexpected error occurred.`, and nothing else |

### Examples

```bash
curl "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
# 200 {"productId":35455,"brandId":1,"priceList":2,...,"amount":25.45,"currency":"EUR"}

curl "http://localhost:8080/prices?applicationDate=2020-06-14%2010:00:00&productId=35455&brandId=1"
# 400 (the space-separated format of the first implementation is no longer accepted)

curl -X POST "http://localhost:8080/prices"
# 405, Allow: GET
```
