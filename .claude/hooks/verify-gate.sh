#!/usr/bin/env bash
# -----------------------------------------------------------------------------------------------
# Quality gate. Runs as a Claude Code *Stop* hook and can also be run by hand:
#     .claude/hooks/verify-gate.sh
#
# Exit 0  -> Claude may stop.
# Exit 2  -> the stop is blocked; everything printed on stderr is fed back to Claude.
#
# Checks, in order:
#   1. Frozen files are identical to git tag "baseline" (catches edits made via Bash too).
#   2. The task list in specs/05-tasks.md is unchanged apart from checkboxes.
#   3. No disabled tests or JUnit assumptions.
#   4. If every task is ticked (= Claude claims to be done): ./mvnw verify must be green.
#
# To avoid an endless loop, after MAX_BLOCKS consecutive blocks the gate lets the session stop so
# that a human can take over.
# -----------------------------------------------------------------------------------------------
set -uo pipefail

ROOT="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"
cd "$ROOT" || exit 0

BASELINE_TAG="baseline"
TASKS_FILE="specs/05-tasks.md"
MAX_BLOCKS=5
COUNTER_FILE=".git/claude-gate-blocks"

# Keep in sync with the "Frozen files" section of CLAUDE.md.
FROZEN_PATHS=(
  pom.xml mvnw mvnw.cmd .mvn .gitignore
  CLAUDE.md README.md .claude scripts baseline
  specs/00-constitution.md specs/01-requirements.md specs/02-domain.md
  specs/03-api-contract.md specs/04-design.md
  src/main/resources/schema.sql src/main/resources/data.sql
  src/main/java/com/seniorglez/prices/PricesApplication.java
  src/test/java/com/seniorglez/prices/acceptance
  src/test/java/com/seniorglez/prices/architecture
)

pass() {
  rm -f "$COUNTER_FILE"
  [[ $# -gt 0 ]] && echo "$*"
  exit 0
}

block() {
  local count=0
  [[ -f "$COUNTER_FILE" ]] && count="$(cat "$COUNTER_FILE" 2>/dev/null || echo 0)"
  count=$((count + 1))
  if (( count > MAX_BLOCKS )); then
    rm -f "$COUNTER_FILE"
    echo "Quality gate still failing after $MAX_BLOCKS attempts. Letting the session stop for human review." >&2
    exit 0
  fi
  echo "$count" > "$COUNTER_FILE"
  {
    echo "QUALITY GATE FAILED (attempt $count of $MAX_BLOCKS). The work is not done."
    echo
    printf '%s\n' "$@"
    echo
    echo "Rules: CLAUDE.md and specs/00-constitution.md. If you believe the specs themselves are wrong,"
    echo "record it in specs/open-questions.md instead of working around them."
  } >&2
  exit 2
}

# 0. No baseline yet: nothing to protect (setup phase).
if ! git rev-parse -q --verify "refs/tags/$BASELINE_TAG" >/dev/null 2>&1; then
  pass
fi

# 1. Frozen files (tracked changes, deletions and new untracked files).
# Claude Code's personal, git-ignored settings file is the only exception.
NOT_FROZEN=(':(exclude).claude/settings.local.json')
changed="$(git diff --name-only "$BASELINE_TAG" -- "${FROZEN_PATHS[@]}" "${NOT_FROZEN[@]}" 2>/dev/null)"
untracked="$(git ls-files --others --exclude-standard -- "${FROZEN_PATHS[@]}" "${NOT_FROZEN[@]}" 2>/dev/null)"
if [[ -n "$changed$untracked" ]]; then
  block "Frozen files differ from tag '$BASELINE_TAG':" \
        ${changed:+"$changed"} ${untracked:+"$untracked"} \
        "" \
        "Restore modified files with: git checkout $BASELINE_TAG -- <path>" \
        "and remove any file you added inside a frozen folder."
fi

# 2. Task list: only checkboxes may change.
normalize_tasks() {
  grep -E '^- \[[ xX]\] \*\*T[0-9]+\*\*' | sed -E 's/^- \[[xX]\]/- [ ]/'
}
if ! diff <(git show "$BASELINE_TAG:$TASKS_FILE" 2>/dev/null | normalize_tasks) \
          <(normalize_tasks < "$TASKS_FILE" 2>/dev/null) >/dev/null; then
  block "The task list in $TASKS_FILE differs from the baseline." \
        "You may only tick checkboxes. Do not add, remove, reorder or reword task lines." \
        "Compare with: git diff $BASELINE_TAG -- $TASKS_FILE"
fi

# 3. Disabled tests or assumptions.
disabled="$(grep -rnE '@Disabled|Assumptions\.|assumeTrue|assumeFalse|assumingThat' src/test 2>/dev/null)"
if [[ -n "$disabled" ]]; then
  block "Disabled tests or JUnit assumptions are forbidden:" "$disabled"
fi

# 4. Not every task is ticked: Claude is pausing (e.g. to ask an open question). Allow it.
if grep -qE '^- \[ \] \*\*T[0-9]+\*\*' "$TASKS_FILE"; then
  pass
fi

# 5. Every task ticked means the work is claimed done, so verify the claim.
if ! output="$(./mvnw -B verify 2>&1)"; then
  block "Every task in $TASKS_FILE is ticked, but './mvnw verify' fails." \
        "Fix the production code (never the frozen tests). Last lines of the build:" \
        "" \
        "$(printf '%s\n' "$output" | tail -n 100)"
fi

pass "Quality gate passed: frozen files intact, task list intact, no disabled tests, ./mvnw verify green."
