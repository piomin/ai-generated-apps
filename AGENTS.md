# Agent Instructions

These rules apply to every AI coding agent working in this repository (e.g. OpenCode, Claude Code).

## Git workflow

- **Never commit or push directly to `master`.** All changes go to a dedicated branch.
- When working on a GitHub issue, create the branch from an up-to-date `master` and make its name unique and traceable to the issue:

  `issue-<issue-number>-<short-kebab-case-description>`

  Example: `issue-6-reviews-pagination`
- If a branch for the issue already exists, continue on it instead of creating a new one.
- Use clear, descriptive commit messages and reference the issue (e.g. `Refs #6`).
- Do not merge the branch yourself. Opening a pull request (with `Closes #<issue-number>` in the description) is welcome, but merging is left to a human.

## Build output

- **Never commit build output**, in particular `target/` directories (compiled classes, jars, `maven-status`, `maven-archiver`, etc.). Only source, test and documentation files belong in a change.
- If `target/` files show up in `git status` (modified, deleted or untracked), leave them out of the commit and do not restore or edit them by hand.

## Issue reporting

After finishing a task that originates from a GitHub issue, **post a comment on that issue** (e.g. with `gh issue comment <number> --body "..."`).

The comment must be very short (2-4 sentences or bullets) and include:
- what was changed, in plain language,
- the name of the branch (and PR link, if one was opened),
- anything left undone or worth a reviewer's attention (e.g. tests not run).

Do not paste diffs or long explanations into the comment.

## Scope

- Implement only what the issue describes. Do not make unrelated refactorings.
- Run the relevant module's tests before declaring the task done, and mention the result in the issue comment.
