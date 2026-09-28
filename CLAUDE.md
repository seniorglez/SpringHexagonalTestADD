# CLAUDE.md

You are implementing a small Spring Boot REST API using **spec-driven development**.
Humans wrote the specs, the seed data and the acceptance/architecture tests. You write the
production code and its unit tests. A build gate, not you, decides when the work is done.

## Commands

| Purpose | Command |
|---|---|
| Every check (compile, all tests, architecture rules, coverage) | `./mvnw verify` |
| One test class | `./mvnw test -Dtest=PriceSelectorTest` |
| Run the app | `./mvnw spring-boot:run` |
| Try it | `curl "http://localhost:8080/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"` |

## Read first, in this order

1. `specs/00-constitution.md`: non-negotiable rules
2. `specs/01-requirements.md`: what the system must do (REQ-xx)
3. `specs/03-api-contract.md`: the HTTP contract
4. `specs/02-domain.md`: domain model, selection rule, seed data
5. `specs/04-design.md`: prescribed packages, classes and wiring
6. `specs/05-tasks.md`: your work queue

The specs are the **only** source of requirements. The original 2023 implementation of this
exercise is not a reference: do not look it up, do not copy it.

## Frozen files: never modify, move or delete them, and never add files inside frozen folders

- `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `.gitignore`
- `CLAUDE.md`, `README.md`, `.claude/` (except your local `settings.local.json`), `scripts/`, `baseline/`
- `specs/00-constitution.md` to `specs/04-design.md`
- `src/main/resources/schema.sql`, `src/main/resources/data.sql`
- `src/main/java/com/seniorglez/prices/PricesApplication.java`
- `src/test/java/com/seniorglez/prices/acceptance/`
- `src/test/java/com/seniorglez/prices/architecture/`

If a frozen file looks wrong, that is an open question (see below), not something for you to fix.

You **may** edit: everything else under `src/`, `specs/05-tasks.md` (checkboxes only),
`specs/open-questions.md` and `docs/`.

## Workflow per task

1. Take the **first unchecked** task in `specs/05-tasks.md`. Work on that task only.
2. Re-read every REQ the task references.
3. Write the unit tests listed in the task's *Done when*. Run them and confirm they fail for the
   expected reason (not because of a compilation error elsewhere).
4. Write the simplest production code that makes them pass. Package and class names are prescribed
   in `specs/04-design.md`.
5. Run `./mvnw verify`.
   - Every test that was green before this task must still be green.
   - Acceptance or architecture tests that the task says turn green must be green.
   - Other acceptance/architecture failures are expected until their task and are not regressions.
6. Tick the checkbox (`- [ ]` becomes `- [x]`). Do not change any other text in the tasks file.
7. Commit: `git add -A && git commit -m "T0N: <summary> [REQ-xx, REQ-yy]"`.

## Definition of done (whole project)

All tasks are ticked and `./mvnw verify` is green.

A **Stop hook** (`.claude/hooks/verify-gate.sh`) runs every time you try to finish. It:

- compares the frozen files with git tag `baseline`,
- checks that the task list was not altered (only checkboxes may change),
- rejects disabled tests and JUnit assumptions,
- runs `./mvnw verify` once every task is ticked.

If the gate blocks you, its output tells you why. Fix the **production code** and continue.

## Never

- Modify frozen files or work around them (excluding tests, overriding surefire/JaCoCo settings
  with system properties, adding a second `@SpringBootApplication`, and so on).
- Use `@Disabled`, JUnit assumptions, or delete or weaken assertions.
- Hard-code seed values or special-case acceptance-test inputs in production code.
- Add dependencies, frameworks or features not in the specs (security, caching, OpenAPI,
  pagination, Lombok, MapStruct...).
- Rewrite git history, create or move tags, or push.
- Guess a requirement.

## When something is unclear or contradictory

1. Add an entry to `specs/open-questions.md` using the template there.
2. Leave the task unchecked and commit.
3. Stop.

Reporting a real ambiguity counts as a success, not a failure.

## Final report

When all tasks are ticked and the gate is green, reply with:

- the tasks completed,
- the number of tests (and how many you wrote),
- the coverage figure,
- any decisions or deviations,
- any open questions.
