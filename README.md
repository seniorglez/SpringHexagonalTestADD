# Price API: redoing a technical test with spec-driven development

Between December 2023 and February 2024 I solved a technical test: a small Spring Boot REST API,
built with hexagonal architecture, that returns the price applicable to a product at a given
moment. That first version is still public at
[seniorglez/SpringHexagonalTest](https://github.com/seniorglez/SpringHexagonalTest).

The new technical chalenge is to **redo the same test using spec-driven development (SDD)**, with
Claude Code acting as the implementer. This repository documents that second attempt. The
interesting part is not only the resulting code but the process: what I decided and wrote myself,
what the AI was allowed to do, and how "done" was verified without trusting the AI's own word.

## The exercise (unchanged)

A `PRICES` table stores, for each brand and product, several price lists. Each price list has a
validity window and a priority. Given a date-time, a product and a brand, the API must return the
single applicable price: when windows overlap, the highest priority wins. The statement provides
four seed rows and five scenarios to test (product 35455, brand 1, on 14, 15 and 16 June 2020).

## What spec-driven means here

1. **Specs first, and specs only.** Everything the system must do is written in `specs/` before
   any production code exists. The AI implements what the specs say. It doesn't infer requirements,
   and it doesn't consult my first implementation.
2. **A human writes the oracle.** The acceptance tests (black-box HTTP) and the architecture rules
   (ArchUnit) are written by me and frozen *before* the AI starts. The AI writes unit tests too, but
   an AI validating itself with tests it wrote itself proves little. The frozen tests are the judge.
3. **A machine decides when the work is done.** A Claude Code *Stop hook* runs a deterministic gate.
   The gate checks that nothing frozen was touched and that `./mvnw verify` is green before the AI
   is allowed to declare the work finished.

| Artefact | Written by | Can the AI change it? |
|---|---|---|
| `specs/00`–`04` (constitution, requirements, domain, API contract, design) | me | no |
| `specs/05-tasks.md` (ordered work queue) | me | tick checkboxes only |
| Acceptance tests and architecture tests | me | no |
| Seed schema and data (`schema.sql`, `data.sql`) | me | no |
| Project skeleton and `pom.xml` (Spring Initializr plus two additions) | Initializr / me | no |
| `CLAUDE.md`, `.claude/` (instructions, permissions, gate) | me | no |
| Production code and unit tests | Claude Code | yes |
| `docs/traceability.md` (requirement → test report) | Claude Code | yes |
| `specs/open-questions.md` | Claude Code asks, I answer | yes |

The boundary is a git tag, `baseline`. Everything before it is human work. Everything after it
is AI work, one commit per task.

## Repository map

```
CLAUDE.md                       instructions Claude Code reads at the start of every session
specs/
  00-constitution.md            non-negotiable principles (architecture, standards, testing rules)
  01-requirements.md            REQ/NFR in EARS style, acceptance criteria, traceability matrix
  02-domain.md                  glossary, model, selection policy, seed data, worked examples
  03-api-contract.md            GET /prices, JSON shape, problem-details errors
  04-design.md                  prescribed packages and classes, wiring, persistence, test strategy
  05-tasks.md                   ten ordered tasks, each with its "done when"
  open-questions.md             where the AI records ambiguities instead of guessing
src/main/resources/
  schema.sql, data.sql          frozen seed
src/test/java/.../acceptance/   frozen black-box HTTP tests (AC-01 … AC-11)
src/test/java/.../architecture/ frozen ArchUnit rules (hexagonal dependency rule, package layout)
.claude/
  settings.json                 permissions (allow/deny) and the Stop hook
  hooks/verify-gate.sh          the quality gate
  commands/next-task.md         /next-task: implement one task
  commands/implement-all.md     /implement-all: implement every remaining task
scripts/bootstrap.sh            generates the starter with Spring Initializr and merges it in
baseline/                       pom additions and starter provenance
```

## How to reproduce

Requirements: JDK 21, git, curl, unzip, bash (Linux, macOS, or WSL/Git Bash on Windows), and Claude Code.

```bash
# 1. Generate the skeleton with Spring Initializr (not with the AI) and add ArchUnit and JaCoCo to the pom
git init
scripts/bootstrap.sh

# 2. Check the baseline: it must compile, and it must be red for the right reasons
./mvnw -q test-compile
./mvnw test        # acceptance tests fail (no /prices yet), architecture tests fail (empty layers)

# 3. Freeze the human baseline
git add -A
git commit -m "chore: human baseline (specs, frozen tests, seed data, starter)"
git tag baseline

# 4. Hand over to Claude Code (from the repository root)
claude
> /implement-all          # or /next-task, one task at a time
```

`bootstrap.sh` calls the Initializr API with fixed parameters (Maven, Java 21, Web, Data JPA, H2,
Validation). It never overwrites an existing file, and it records the Spring Boot version it got
in `baseline/STARTER.md`.

## Guardrails

Instructions alone are not enough, so there are three layers:

1. **Instructions:** `CLAUDE.md` and the constitution explain the workflow and what is frozen.
   They are necessary, but a model can drift or forget them in a long session.
2. **Permissions:** `.claude/settings.json` denies edits to frozen paths and forbids history
   rewriting (`git tag`, `reset`, `rebase`, `push`, `commit --amend`). These rules apply to Claude's
   file-editing tools. They cannot stop every possible shell command.
3. **Deterministic gate:** `.claude/hooks/verify-gate.sh` runs as a Stop hook. It compares frozen
   paths with the `baseline` tag, so an edit made via the shell is caught too. It rejects changes
   to the task list other than ticked checkboxes, rejects `@Disabled` and assumptions, and, once
   every task is ticked, requires `./mvnw verify` to be green (all tests, ArchUnit, JaCoCo ≥ 80%).
   Exit code 2 blocks the stop and sends the reason back to Claude. After five consecutive blocks
   it lets the session end so that a human can step in.

## How to review the result

```bash
git log --oneline baseline..HEAD        # one commit per task: "T0N: … [REQ-xx]"
git diff --stat baseline                # only non-frozen paths should appear
.claude/hooks/verify-gate.sh            # run the same gate by hand
./mvnw verify                           # full build; coverage report in target/site/jacoco
./mvnw spring-boot:run
curl "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

Then read `docs/traceability.md` (every requirement mapped to the tests that verify it) and
`specs/open-questions.md`.
