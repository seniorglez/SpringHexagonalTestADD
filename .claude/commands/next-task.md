---
description: Implement the next unchecked task from specs/05-tasks.md (one task only)
---
Read `CLAUDE.md` and every file in `specs/`.

Implement **one** task: $ARGUMENTS
If no task ID was given, take the first unchecked task in `specs/05-tasks.md`.

Follow the "Workflow per task" in `CLAUDE.md` exactly: tests first, then the simplest code, then
`./mvnw verify`, then tick the checkbox, then commit.

When finished, report the task ID, the requirements covered, the tests you added, and the
`./mvnw verify` result. Then stop and do not start the next task.
