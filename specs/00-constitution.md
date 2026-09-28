# 00 · Constitution

These principles are **non-negotiable** and take precedence over every other spec. If a lower-level
spec contradicts this file, this file wins and the contradiction goes into `open-questions.md`.

## 1. Source of truth

1. Precedence, highest first: `00-constitution` > `01-requirements` > `03-api-contract` >
   `02-domain` > `04-design` > `05-tasks`.
2. Nothing outside `/specs` is a requirement: not the original implementation of this exercise,
   not "common practice", not assumptions.
3. Implement exactly what is specified. No extra endpoints or features: no write operations,
   security, caching, OpenAPI/Swagger, pagination, actuator or HATEOAS.

## 2. Architecture: hexagonal (ports and adapters)

1. **Dependency rule:** `infrastructure → application → domain`. Dependencies never point outwards.
2. **domain** is pure Java 21. No Spring, JPA, Jackson, Bean Validation, Lombok or logging
   frameworks. It contains the model, the business rules and domain exceptions.
3. **application** is pure Java 21 and depends only on `domain`. It defines the **ports**:
   - inbound ports: use-case interfaces that drivers (e.g. the REST adapter) call;
   - outbound ports: interfaces that the application needs and infrastructure implements.
4. **infrastructure** holds all framework code. Adapters implement or call ports. Beans for
   framework-free classes are declared explicitly in `infrastructure.config`. No custom
   component-scanning tricks.
5. Adapters never depend on each other. Inbound adapters depend on inbound ports, never on
   application services.
6. Business rules (applicability, priority, tie-break) live in the domain. Adapters may pre-filter
   for efficiency, but correctness must never depend on an adapter.

These rules are enforced by the frozen architecture tests.

## 3. Code standards

- Java 21. Use records for immutable data (value objects, queries, DTOs).
- Money is `java.math.BigDecimal`, never `float` or `double`.
- Dates are `java.time.LocalDateTime` (the API has no time zones; see the API contract).
- Constructor injection only. No field injection.
- Domain objects validate their invariants in the constructor and fail fast
  (`Objects.requireNonNull`, `IllegalArgumentException`).
- Mapping between layers is hand-written. No Lombok, no MapStruct.
- No new dependencies. `pom.xml` is frozen.
- English everywhere. No commented-out code. No TODOs left in finished tasks.
- Prefer the simplest code that satisfies the specs and the tests.

## 4. Testing

1. The frozen **acceptance tests** (HTTP, black-box) and **architecture tests** are the oracle.
   Unit tests you write are for design feedback and regression safety. They never replace the oracle.
2. TDD per task: write the failing test first, then the code.
3. Domain and application unit tests run without a Spring context. Prefer hand-written fakes for
   ports over mocks.
4. Test names describe behaviour (`selectsHighestPriorityWhenWindowsOverlap`), not methods.
5. A green test must stay green. Regressions are never acceptable, even temporarily across a commit.
6. **Forbidden:**
   - `@Disabled`, JUnit assumptions, deleting or weakening assertions;
   - catching exceptions only to make a test pass;
   - hard-coding seed or acceptance values in production code;
   - special-casing test inputs;
   - changing build configuration to skip or exclude checks.

## 5. When in doubt

Do not guess. Record the question in `specs/open-questions.md`, leave the task unchecked, commit
and stop.

## 6. Frozen artifacts

Frozen artifacts are listed in `CLAUDE.md`. Only a human changes them, by creating a new
`baseline` tag. The Stop-hook gate verifies them against that tag.
