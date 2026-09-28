---
description: Implement every remaining task in specs/05-tasks.md, in order
---
Read `CLAUDE.md` and every file in `specs/`.

Implement all remaining tasks in `specs/05-tasks.md`, **in order and one at a time**, following the
"Workflow per task" in `CLAUDE.md`. Each task ends with `./mvnw verify`, a ticked checkbox and a commit
before you move on to the next one.

Do not stop between tasks unless a spec is unclear or contradictory. In that case record it in
`specs/open-questions.md`, commit and stop.

When every task is ticked, the Stop hook runs the quality gate. If it blocks you, fix the cause and
continue. Finish with the final report described in `CLAUDE.md`.
